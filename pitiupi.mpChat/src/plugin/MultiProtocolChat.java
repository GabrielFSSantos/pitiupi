package plugin;

import GUI.MulticastMpChatMenu;
import GUI.PrivateChatWindow;
import GUI.PublicChatWindow;
import GUI.UnicastMpChatMenu;
import net.MpChatMessage;
import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Messages.Message;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.models.Plugin;
import pitiupi.plugin.Pitiupi;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.List;

public class MultiProtocolChat extends Plugin {
    Pitiupi pitiupi;

    PublicChatWindow publicChatWindow;
    PrivateChatWindow privateChatWindow;

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
    public void createPlugin(Pitiupi pitiupi) {
        this.pitiupi = pitiupi;

        publicChatWindow = new PublicChatWindow(pitiupi);
        privateChatWindow = new PrivateChatWindow(pitiupi);

        MulticastMpChatMenu multicastMpChatMenu = new MulticastMpChatMenu(publicChatWindow);
        UnicastMpChatMenu unicastMpChatMenu = new UnicastMpChatMenu(this);

        pitiupi.createMenu(MenuCategory.Social, multicastMpChatMenu);
        pitiupi.createMenu(MenuCategory.Social, unicastMpChatMenu);
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
            privateChatWindow.addConnection(connection);
        });
        return true;
    }

    @Override
    public boolean receiveBytes(TcpConnection connection, byte[] bytes) {
        MpChatMessage mpChatMessage = parseMessage(bytes);
        privateChatWindow.receiveMessage(connection, mpChatMessage);
        return true;
    }

    @Override
    public boolean onSocketClosed(TcpConnection connection) {
        privateChatWindow.removeConnection(connection);
        return true;
    }

    public void openConnection(String ip) {
        try {
            pitiupi.connectTcp(InetAddress.getByName(ip), getIdentifier(), getIdentifier());
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
