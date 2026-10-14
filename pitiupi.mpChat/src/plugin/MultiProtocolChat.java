package plugin;

import GUI.MulticastMpChatMenu;
import GUI.PublicChatWindow;
import GUI.UnicastMpChatMenu;
import control.PrivateChatController;
import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.List;

public class MultiProtocolChat extends Plugin {
    private ConnectionManager connectionManager;

    PrivateChatController privateChatController;
    PublicChatWindow publicChatWindow;

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

        privateChatController = new PrivateChatController(connectionManager, this);

        publicChatWindow = new PublicChatWindow(connectionManager);

        MulticastMpChatMenu multicastMpChatMenu = new MulticastMpChatMenu(publicChatWindow);
        UnicastMpChatMenu unicastMpChatMenu = new UnicastMpChatMenu(privateChatController);

        guiManager.createMenu(MenuCategory.Social, multicastMpChatMenu);
        guiManager.createMenu(MenuCategory.Social, unicastMpChatMenu);
    }

    @Override
    public List<Class<? extends Message>> getMessages() {
        return List.of(MpChatMessage.class);
    }

    @Override
    public void receiveMessage(Message message) {
        if (message instanceof MpChatMessage mpChatMessage) {
            publicChatWindow.receiveMessage(mpChatMessage);
        }
    }


    @Override
    public boolean receivePrivateConnection(TcpConnection connection) {
        SwingUtilities.invokeLater(() -> {
            privateChatController.connectionReceived(connection);
        });
        return true;
    }

    @Override
    public boolean receiveBytes(TcpConnection connection, byte[] bytes) {
        MpChatMessage mpChatMessage = parseMessage(bytes);
        if (mpChatMessage == null) {
            System.out.println("Invalid message received");
            return true;
        }
        else System.out.println(mpChatMessage.getMessage());

        privateChatController.receiveMessage(connection, mpChatMessage);
        return true;
    }

    @Override
    public boolean onSocketClosed(TcpConnection connection) {
        privateChatController.closeChat(connection);
        return true;
    }

    public void openConnection(String ip) {
        try {
            connectionManager.connectPluginTcp(
                    InetAddress.getByName(ip),
                    getIdentifier(),
                    getIdentifier()
            );
        }
        catch (Exception e) {
            System.out.println("Error opening connection: " + e.getMessage());
        }
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
