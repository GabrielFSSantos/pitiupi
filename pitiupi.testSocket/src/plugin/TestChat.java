package plugin;

import GUI.ChatMenu;
import control.ChatController;
import net.TestChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.net.Message;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.plugin.PrivateConnectionPlugin;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public class TestChat implements PrivateConnectionPlugin{
    ChatController chatController;
    ConnectionManager connectionManager;
    GuiManager guiManager;

    @Override
    public String getName() {
        return "MultiProtocol chat";
    }

    @Override
    public String getAuthor() {
        return "Gustavo Henrique";
    }

    @Override
    public String getVersion() {
        return "0";
    }

    @Override
    public String getIdentifier() {
        return "Multi.Protocol.Chat.Pitiupi";
    }

    @Override
    public void createPlugin(ConnectionManager connectionManager, GuiManager guiManager) {
        this.connectionManager = connectionManager;
        this.guiManager = guiManager;

        ChatMenu chatMenu = new ChatMenu(this);
        chatController = new ChatController(connectionManager, chatMenu);
        guiManager.addMenu(chatMenu);
    }

    @Override
    public Message getMessage() {
        return new TestChatMessage();
    }

    @Override
    public void receiveMessage(byte[] message) {
        TestChatMessage testChatMessage = parseMessage(message);
        chatController.receivePublic(testChatMessage);
    }


    @Override
    public void receivePrivateConnection(TcpConnection tcpConnection) {
        SwingUtilities.invokeLater(() -> {
            chatController.addChatWindow(tcpConnection);
        });
    }

    @Override
    public void receiveBytes(TcpConnection connection, byte[] bytes) {
        TestChatMessage testChatMessage = parseMessage(bytes);
        chatController.receivePrivate(connection, testChatMessage);
    }

    @Override
    public void onSocketClosed(TcpConnection tcpConnection) {
        chatController.removeChatWindow(tcpConnection);
    }

    public boolean openConnection(String ip) {
        try {
            connectionManager.connectPluginTcp(InetAddress.getByName(ip), getIdentifier(), getIdentifier());
            return true;
        }
        catch (Exception e) {
            System.out.println("Error opening connection: " + e.getMessage());
        }
        return false;
    }

    private TestChatMessage parseMessage(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            if (ois.readObject() instanceof TestChatMessage msg) {
                return msg;
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }
}
