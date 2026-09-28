package pitiupi.control;

import pitiupi.GUI.MainWindow;
import pitiupi.net.HeartbeatMessage;
import pitiupi.net.Message;
import pitiupi.net.tcp.SocketTCP;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.net.udp.SocketUDP;

import java.net.InetAddress;

public class ConnectionManager {
    private final String pitiupiIdentifier;

    private PluginManager pluginManager;

    private HeartbeatManager heartbeatManager;

    private SocketTCP socketTCP;
    private SocketUDP socketUDP;
    private int port;

    public ConnectionManager(MainWindow main, PluginManager pluginManager, int port) {
        this.pluginManager = pluginManager;
        this.port = port;

        pitiupiIdentifier = "Pitiupi.Alice.2026";

        reconnect();

        heartbeatManager = new HeartbeatManager(main, this);
        heartbeatManager.start();
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
        } catch (Exception ex) {
            return false;
        }
    }

    public void broadcastLocal(Message message) {
        pluginManager.handlePublicMessage(message);
    }

    public boolean sendLocal(byte[] message, String receiverId) {
        return pluginManager.handlePrivateMessage(message, receiverId);
    }

    public void addPeerListener(PeerListener peerListener) {
        heartbeatManager.addPeerListener(peerListener);
    }

    public void receiveHeartbeat(HeartbeatMessage message, InetAddress destinationAddress) {
        heartbeatManager.receiveHeartbeat(message, destinationAddress);
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
}
