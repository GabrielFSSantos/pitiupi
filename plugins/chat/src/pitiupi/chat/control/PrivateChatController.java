package pitiupi.chat.control;

import pitiupi.chat.net.LegacyMpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.models.User;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.presentation.shell.LocalNetworkInfo;
import pitiupi.presentation.shell.PeerIpv4;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrivateChatController {

    private final ConnectionManager connectionManager;
    private final PrivateChatHost host;
    private final Map<String, TcpConnection> connections = new HashMap<>();
    private final Set<String> pendingConnections = new HashSet<>();
    private final Map<String, PendingSend> pendingTextByAddress = new HashMap<>();
    private PrivateChatUiSink uiSink;

    public PrivateChatController(ConnectionManager connectionManager, PrivateChatHost host) {
        this.connectionManager = connectionManager;
        this.host = host;
    }

    public void setUiSink(PrivateChatUiSink uiSink) {
        this.uiSink = uiSink;
    }

    private static String addressKey(TcpConnection connection) {
        return PeerIpv4.fromInetAddress(connection.getAddress());
    }

    public void openChat(User user) {
        if (user == null || user.getAddress() == null) {
            return;
        }
        String address = PeerIpv4.fromInetAddress(user.getAddress());
        TcpConnection existing = connections.get(address);
        if (existing != null && !existing.isClosed()) {
            flushPendingSend(address, user);
            return;
        }
        if (pendingConnections.contains(address)) {
            return;
        }
        pendingConnections.add(address);
        host.openConnection(address);
    }

    public void registerOutgoingConnection(String address, TcpConnection connection) {
        if (address == null || connection == null) {
            return;
        }
        String canonical = PeerIpv4.normalize(address);
        pendingConnections.remove(canonical);
        replaceConnection(canonical, connection, false);
        User user = new User("", connection.getAddress(), System.currentTimeMillis(), null);
        flushPendingSend(canonical, user);
    }

    public void receiveMessage(TcpConnection connection, LegacyMpChatMessage message) {
        if (uiSink != null) {
            uiSink.appendPrivateLine(addressKey(connection), message, false);
        }
    }

    public void connectionReceived(TcpConnection connection) {
        String address = addressKey(connection);
        pendingConnections.remove(address);
        replaceConnection(address, connection, true);
    }

    private void replaceConnection(String address, TcpConnection connection, boolean notifyReady) {
        TcpConnection existing = connections.get(address);
        if (existing != null && !existing.isClosed() && existing != connection) {
            return;
        }
        boolean first = existing == null || existing.isClosed();
        connections.put(address, connection);
        if (notifyReady && first && uiSink != null) {
            uiSink.onPrivateChannelReady(address);
        }
    }

    public void closeChat(TcpConnection connection) {
        String address = addressKey(connection);
        connections.remove(address);
        pendingConnections.remove(address);
        pendingTextByAddress.remove(address);
        if (uiSink != null) {
            uiSink.onPrivateChannelClosed(address);
        }
    }

    public boolean sendText(User user, String text) {
        if (user == null || user.getAddress() == null || text == null || text.isBlank()) {
            return false;
        }
        String address = PeerIpv4.fromInetAddress(user.getAddress());
        TcpConnection connection = connections.get(address);
        if (connection == null || connection.isClosed()) {
            if (pendingConnections.contains(address)) {
                pendingTextByAddress.put(address, new PendingSend(user, text));
                return true;
            }
            return false;
        }
        return sendOnConnection(connection, address, text);
    }

    private void flushPendingSend(String address, User user) {
        PendingSend pending = pendingTextByAddress.remove(address);
        if (pending != null) {
            sendText(user != null ? user : pending.user, pending.text);
        }
    }

    private boolean sendOnConnection(TcpConnection connection, String address, String text) {
        try {
            LegacyMpChatMessage message = new LegacyMpChatMessage(
                    connectionManager.getUserName(),
                    LocalNetworkInfo.bestLocalIpv4(),
                    "TCP privado",
                    text
            );
            connection.send(message.toByteArray());
            if (uiSink != null) {
                uiSink.appendPrivateLine(address, message, true);
            }
            return true;
        } catch (Exception ex) {
            System.out.println("Error sending private TCP: " + ex.getMessage());
            return false;
        }
    }

    public boolean hasOpenChannel(User user) {
        if (user == null || user.getAddress() == null) {
            return false;
        }
        TcpConnection connection = connections.get(PeerIpv4.fromInetAddress(user.getAddress()));
        return connection != null && !connection.isClosed();
    }

    private record PendingSend(User user, String text) {
    }
}
