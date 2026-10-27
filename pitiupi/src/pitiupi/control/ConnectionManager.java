package pitiupi.control;

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
import pitiupi.net.tcp.TcpConnection;
import pitiupi.net.udp.SocketUDP;

import java.net.InetAddress;
import java.util.function.Consumer;

public class ConnectionManager {
    private final String pitiupiIdentifier;

    private final PluginManager pluginManager;

    private final HeartbeatManager heartbeatManager;
    private final PresenceService presenceService;
    private final PeerActionBroker peerActionBroker;
    private final InviteInboxService inviteInboxService;
    private final PeerActivityInbox peerActivityInbox;

    private SocketTCP socketTCP;
    private SocketUDP socketUDP;
    private String username;
    private int port;
    private volatile boolean shutdownComplete;
    private Consumer<String> inviteAcceptedUiListener = pluginId -> {
    };

    public ConnectionManager(String username, PluginManager pluginManager, int port) {
        this.pluginManager = pluginManager;

        this.username = username;
        this.port = port;

        pitiupiIdentifier = "Pitiupi.Alice.2026";

        reconnect();

        heartbeatManager = new HeartbeatManager(this);
        peerActionBroker = new PeerActionBroker();
        inviteInboxService = new InviteInboxService(heartbeatManager);
        peerActivityInbox = new PeerActivityInbox();
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
        if (socketUDP != null) {
            socketUDP.close();
        }
        if (socketTCP != null) {
            socketTCP.close();
        }
    }

    public TcpConnection connectPluginTcp(InetAddress destinationAdress, String senderId, String receiverId) {
        return socketTCP.createPrivateConnection(destinationAdress, senderId, receiverId);
    }

    public boolean sendTcp(Message message, InetAddress destinationAddress) {
        return socketTCP.send(message, destinationAddress);
    }

    public boolean sendUdpUnicast(Message message, InetAddress destinationAddress) {
        try {
            socketUDP.sendUnicast(message, destinationAddress);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean sendUdpMulticast(Message message) {
        try {
            socketUDP.sendMulticast(message);
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

    public void setPort(int port) {
        this.port = port;
        reconnect();
    }

    public int getPort() {
        return port;
    }

    public String getIdentifier(){
        return this.pitiupiIdentifier;
    }

    private void reconnect() {
        if (this.socketUDP != null) {
            this.socketUDP.close();
        }
        if (this.socketTCP != null) {
            this.socketTCP.close();
        }

        this.socketUDP = new SocketUDP(this, pluginManager);
        this.socketTCP = new SocketTCP(this, pluginManager);

        this.socketUDP.start();
        this.socketTCP.start();
    }

    public String getUserName() {
        return this.username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }
}
