package pitiupi.control;

import pitiupi.application.activity.PeerActivityFocus;
import pitiupi.application.activity.PeerActivityInbox;
import pitiupi.application.invites.InviteInboxService;
import pitiupi.application.peeractions.PeerActionBroker;
import pitiupi.application.presence.PresenceService;
import pitiupi.models.Messages.SessionInviteMessage;
import pitiupi.control.plugins.PluginManager;
import pitiupi.models.Messages.GoodbyeMessage;
import pitiupi.models.Messages.HeartbeatMessage;
import pitiupi.models.Messages.Message;
import pitiupi.net.tcp.SocketTCP;
import pitiupi.net.tcp.TcpAttemptListener;
import pitiupi.net.tcp.TcpConnectNotice;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.net.udp.SocketUDP;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.NetworkPreferences;
import pitiupi.presentation.shell.PeerIpv4;

import javax.swing.SwingUtilities;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ConnectionManager {
    private final String pitiupiIdentifier;

    private final PluginManager pluginManager;

    private final HeartbeatManager heartbeatManager;
    private final PresenceService presenceService;
    private final PeerActionBroker peerActionBroker;
    private final InviteInboxService inviteInboxService;
    private final PeerActivityInbox peerActivityInbox;
    private final PeerActivityFocus peerActivityFocus;

    private final Object socketsLock = new Object();
    private final ExecutorService tcpConnectPool = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "pitiupi-tcp-connect");
        thread.setDaemon(true);
        return thread;
    });

    private SocketTCP socketTCP;
    private SocketUDP socketUDP;
    private String username;
    private int port;
    private int multicastTtl;
    private volatile boolean shutdownComplete;
    private Consumer<String> inviteAcceptedUiListener = pluginId -> {
    };

    public ConnectionManager(String username, PluginManager pluginManager, int port) {
        this.pluginManager = pluginManager;

        this.username = username;
        this.port = port;
        NetworkPreferences.applyEgressBeforeSockets();
        this.multicastTtl = NetworkPreferences.loadTtl();

        pitiupiIdentifier = "Pitiupi.Alice.2026";

        String bindError = openSockets();
        if (bindError != null) {
            System.err.println(bindError);
        }

        heartbeatManager = new HeartbeatManager(this);
        peerActionBroker = new PeerActionBroker();
        inviteInboxService = new InviteInboxService(heartbeatManager);
        peerActivityInbox = new PeerActivityInbox();
        peerActivityFocus = new PeerActivityFocus();
        peerActivityInbox.setFocus(peerActivityFocus);
        presenceService = new PresenceService(heartbeatManager, inviteInboxService, peerActivityInbox);
        inviteInboxService.setOnNotificationsChanged(heartbeatManager::notifyPeerListeners);
        peerActivityInbox.setOnChanged(heartbeatManager::notifyPeerListeners);
        heartbeatManager.start();

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownGracefully, "pitiupi-goodbye"));
    }

    public PresenceService getPresenceService() {
        return presenceService;
    }

    public PeerActionBroker getPeerActionBroker() {
        return peerActionBroker;
    }

    public InviteInboxService getInviteInboxService() {
        return inviteInboxService;
    }

    public PeerActivityInbox getPeerActivityInbox() {
        return peerActivityInbox;
    }

    public PeerActivityFocus getPeerActivityFocus() {
        return peerActivityFocus;
    }

    public void setInviteAcceptedUiListener(Consumer<String> listener) {
        this.inviteAcceptedUiListener = listener != null ? listener : pluginId -> {
        };
    }

    public void notifyInviteAcceptedForUi(String pluginId) {
        if (pluginId != null) {
            inviteAcceptedUiListener.accept(pluginId);
        }
    }

    public HeartbeatManager getHeartbeatManager() {
        return heartbeatManager;
    }

    public void receiveSessionInvite(SessionInviteMessage message) {
        inviteInboxService.receive(message);
    }

    public void shutdownGracefully() {
        if (shutdownComplete) {
            return;
        }
        shutdownComplete = true;
        heartbeatManager.sendGoodbyeAndStop();
        closeSockets();
        tcpConnectPool.shutdownNow();
    }

    public int getMulticastTtl() {
        return multicastTtl;
    }

    /**
     * Sai da sala atual, reabre UDP e TCP e anuncia de novo.
     *
     * @return {@code null} quando a sala nova ficou de pé; mensagem de erro quando a configuração anterior foi mantida.
     */
    public String applyNetwork(int newPort, int newTtl, LocalNetworkInfo.EgressMode mode) {
        if (newPort < 1 || newPort > 65535) {
            return "Porta inválida.";
        }
        LocalNetworkInfo.EgressMode requested = mode == null ? LocalNetworkInfo.EgressMode.AUTO : mode;
        if (!LocalNetworkInfo.canUse(requested)) {
            if (requested == LocalNetworkInfo.EgressMode.WIRED) {
                return "Não há interface de cabo disponível.";
            }
            return "Não há interface de Wi-Fi disponível.";
        }
        int ttl = NetworkPreferences.clampTtl(newTtl);
        synchronized (socketsLock) {
            int oldPort = this.port;
            int oldTtl = this.multicastTtl;
            LocalNetworkInfo.EgressMode oldMode = LocalNetworkInfo.getEgressMode();

            heartbeatManager.sendGoodbye();
            heartbeatManager.clearPeers();
            closeSockets();

            this.port = newPort;
            this.multicastTtl = ttl;
            LocalNetworkInfo.setEgressMode(requested);
            String error = openSockets();
            if (error != null) {
                this.port = oldPort;
                this.multicastTtl = oldTtl;
                LocalNetworkInfo.setEgressMode(oldMode);
                String rollback = openSockets();
                heartbeatManager.announceNow();
                if (rollback != null) {
                    return error + " Também não foi possível restaurar a porta " + oldPort + ".";
                }
                return error;
            }
            heartbeatManager.announceNow();
            return null;
        }
    }

    public TcpConnection connectPluginTcp(InetAddress destinationAdress, String senderId, String receiverId) {
        SocketTCP tcp = currentTcp();
        if (tcp == null) {
            return null;
        }
        return tcp.createPrivateConnection(destinationAdress, senderId, receiverId);
    }

    public void connectPluginTcpAsync(String ipText, String senderId, String receiverId, TcpAttemptListener listener) {
        tcpConnectPool.execute(() -> {
            InetAddress address = resolve(ipText);
            String endpoint = endpointLabel(address, ipText);
            if (address == null) {
                deliverFailure(listener, endpoint);
                return;
            }
            deliverConnecting(listener, endpoint);
            TcpConnection connection = connectPluginTcp(address, senderId, receiverId);
            if (connection == null) {
                deliverFailure(listener, endpoint);
                return;
            }
            deliverSuccess(listener, connection);
        });
    }

    public boolean sendTcp(Message message, InetAddress destinationAddress) {
        SocketTCP tcp = currentTcp();
        if (tcp == null) {
            return false;
        }
        return tcp.send(message, destinationAddress);
    }

    public void sendTcpAsync(Message message, String ipText, TcpAttemptListener listener) {
        tcpConnectPool.execute(() -> {
            InetAddress address = resolve(ipText);
            String endpoint = endpointLabel(address, ipText);
            if (address == null) {
                deliverFailure(listener, endpoint);
                return;
            }
            SocketTCP tcp = currentTcp();
            boolean live = tcp != null && tcp.hasOpenPublicConnection(address);
            if (TcpConnectNotice.shouldShowConnecting(live)) {
                deliverConnecting(listener, endpoint);
            }
            boolean sent = tcp != null && tcp.send(message, address);
            if (sent) {
                deliverSuccess(listener, null);
                return;
            }
            deliverFailure(listener, endpoint);
        });
    }

    public boolean sendUdpUnicast(Message message, InetAddress destinationAddress) {
        SocketUDP udp = currentUdp();
        if (udp == null) {
            return false;
        }
        try {
            udp.sendUnicast(message, destinationAddress);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean sendUdpMulticast(Message message) {
        SocketUDP udp = currentUdp();
        if (udp == null) {
            return false;
        }
        try {
            udp.sendMulticast(message);
            return true;
        } catch (Exception e) {
            System.err.println("Error sending UDP Multicast");
        }
        return false;
    }

    public void broadcastLocal(byte[] message) {
        pluginManager.handleMessage(message);
    }

    public void addPeerListener(PeerListener peerListener) {
        heartbeatManager.addPeerListener(peerListener);
    }

    public void receiveHeartbeat(HeartbeatMessage message, InetAddress destinationAddress) {
        presenceService.onHeartbeat(message, destinationAddress);
    }

    public void receiveGoodbye(GoodbyeMessage message) {
        presenceService.onGoodbye(message);
    }

    public boolean setPort(int port) {
        return applyNetwork(port, multicastTtl, LocalNetworkInfo.getEgressMode()) == null;
    }

    public int getPort() {
        return port;
    }

    public String getIdentifier(){
        return this.pitiupiIdentifier;
    }

    private SocketUDP currentUdp() {
        synchronized (socketsLock) {
            return socketUDP;
        }
    }

    private SocketTCP currentTcp() {
        synchronized (socketsLock) {
            return socketTCP;
        }
    }

    private void closeSockets() {
        SocketUDP udp;
        SocketTCP tcp;
        synchronized (socketsLock) {
            udp = socketUDP;
            tcp = socketTCP;
            socketUDP = null;
            socketTCP = null;
        }
        if (udp != null) {
            udp.close();
            joinQuiet(udp);
        }
        if (tcp != null) {
            tcp.close();
            joinQuiet(tcp);
        }
    }

    private String openSockets() {
        SocketUDP udp = new SocketUDP(this, pluginManager);
        SocketTCP tcp = new SocketTCP(this, pluginManager);
        if (!udp.isJoined() || !tcp.isAccepting()) {
            udp.close();
            tcp.close();
            return "Não foi possível abrir a porta " + port + ". Ela pode estar em uso.";
        }
        synchronized (socketsLock) {
            socketUDP = udp;
            socketTCP = tcp;
        }
        udp.start();
        tcp.start();
        return null;
    }

    private static void joinQuiet(Thread thread) {
        try {
            thread.join(1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static InetAddress resolve(String ipText) {
        if (ipText == null || ipText.isBlank()) {
            return null;
        }
        try {
            return InetAddress.getByName(ipText.trim());
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private String endpointLabel(InetAddress address, String ipText) {
        String host = address != null ? PeerIpv4.fromInetAddress(address) : (ipText == null ? "" : ipText.trim());
        return host + ":" + getPort();
    }

    private static void deliverConnecting(TcpAttemptListener listener, String endpoint) {
        if (listener == null) {
            return;
        }
        onEdt(() -> listener.onConnecting(endpoint));
    }

    private static void deliverSuccess(TcpAttemptListener listener, TcpConnection connection) {
        if (listener == null) {
            return;
        }
        onEdt(() -> listener.onSuccess(connection));
    }

    private static void deliverFailure(TcpAttemptListener listener, String endpoint) {
        if (listener == null) {
            return;
        }
        onEdt(() -> listener.onFailure(TcpConnectNotice.failureMessage(endpoint)));
    }

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    public String getUserName() {
        return this.username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
}
