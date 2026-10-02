package plugin;

import GUI.ChatMenu;
import control.ChatController;
import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Messages.Message;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.models.Plugin;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public class MultiProtocolChat extends Plugin {
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
        guiManager.addMulticastMenu(chatMenu);
    }

    @Override
    public Message getMessage() {
        return new MpChatMessage();
    }

    @Override
    public void receiveMessage(byte[] message) {
        MpChatMessage mpChatMessage = parseMessage(message);
        chatController.receivePublic(mpChatMessage);
    }

    @Override
    public boolean receivePrivateConnection(TcpConnection connection) {
        SwingUtilities.invokeLater(() -> {
            chatController.addChatWindow(connection);
        });
        return true;
    }

    @Override
    public boolean receiveBytes(TcpConnection connection, byte[] bytes) {
        MpChatMessage mpChatMessage = parseMessage(bytes);
        chatController.receivePrivate(connection, mpChatMessage);
        return true;
    }

    @Override
    public boolean onSocketClosed(TcpConnection connection) {
        chatController.removeChatWindow(connection);
        return true;
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

    private MpChatMessage parseMessage(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(bis);
            if (ois.readObject() instanceof MpChatMessage msg) {
                return msg;
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println(ex.toString());
        }
        return null;
    }
}
