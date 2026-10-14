package control;

import GUI.PrivateChatWindow;
import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.models.User;
import pitiupi.net.tcp.TcpConnection;
import plugin.MultiProtocolChat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PrivateChatController {

    private final ConnectionManager connectionManager;
    private final MultiProtocolChat multiProtocolChat;

    /*
     * Relaciona o endereço do usuário à janela de chat.
     */
    private final Map<String, PrivateChatWindow> windows;

    /*
     * Endereços para os quais uma conexão já foi solicitada,
     * mas ainda não chegou.
     */
    private final Set<String> pendingConnections;

    public PrivateChatController(
            ConnectionManager connectionManager,
            MultiProtocolChat multiProtocolChat
    ) {
        this.connectionManager = connectionManager;
        this.multiProtocolChat = multiProtocolChat;

        this.windows = new HashMap<>();
        this.pendingConnections = new HashSet<>();
    }

    private static String addressKey(TcpConnection connection) {
        return connection.getAddress().getHostAddress();
    }

    public void openChat(User user) {
        String address = user.getAddress().getHostAddress();

        PrivateChatWindow window = windows.get(address);

        /*
         * Já existe uma janela para esse usuário.
         */
        if (window != null) {
            window.setVisible(true);
            window.toFront();
            window.requestFocus();
            return;
        }

        /*
         * Já estamos esperando uma conexão para esse usuário.
         */
        if (pendingConnections.contains(address)) {
            return;
        }

        /*
         * Ainda não existe conexão.
         */
        pendingConnections.add(address);

        multiProtocolChat.openConnection(address);
    }

    public void receiveMessage(TcpConnection connection, MpChatMessage message) {
        String address = addressKey(connection);

        PrivateChatWindow window = windows.get(address);

        if (window == null) {
            return;
        }

        window.receiveMessage(message);
    }

    /**
     * Deve ser chamado quando uma nova conexão privada chegar.
     */
    public void connectionReceived(TcpConnection connection) {
        String address = addressKey(connection);

        pendingConnections.remove(address);

        PrivateChatWindow window = new PrivateChatWindow(
                connectionManager,
                connection
        );

        windows.put(address, window);

        window.setVisible(true);
        window.toFront();
        window.requestFocus();
    }

    /**
     * Fecha a janela e remove o relacionamento.
     */
    public void closeChat(TcpConnection connection) {
        String address = addressKey(connection);

        PrivateChatWindow window = windows.remove(address);

        if (window != null) {
            window.dispose();
        }
    }
}
