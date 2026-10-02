package control;

import GUI.ChatMenu;
import GUI.ChatWindow;
import GUI.PrivateChatWindow;
import GUI.PublicChatWindow;
import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.control.PeerListener;
import pitiupi.models.User;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;

public class ChatController implements PeerListener {
    ConnectionManager connectionManager;
    ChatMenu chatMenu;

    PublicChatWindow publicChatWindow;

    private final HashMap<TcpConnection, ChatWindow>  chatWindows;

    public ChatController(ConnectionManager connectionManager, ChatMenu chatMenu) {
        this.connectionManager = connectionManager;
        publicChatWindow = new PublicChatWindow(connectionManager);

        chatMenus = new HashMap<>();
        chatWindows = new HashMap<>();

        this.chatMenu = chatMenu;

        this.chatMenu.newMenuItem(new JMenuItem("Show global chat"), publicChatWindow);
    }

    public void addChatWindow(TcpConnection tcpConnection) {
        if (chatWindows.containsKey(tcpConnection)) return;

        PrivateChatWindow newChat = new PrivateChatWindow(connectionManager, tcpConnection);
        chatWindows.put(tcpConnection, newChat);
        JMenuItem newMenuItem = new JMenuItem();
        chatMenus.put(tcpConnection, newMenuItem);
        newMenuItem.setText(tcpConnection.getAddress().toString());
        chatMenu.newMenuItem(newMenuItem, newChat);
    }

    public void removeChatWindow(TcpConnection tcpConnection) {
        ChatWindow removedWindow = chatWindows.remove(tcpConnection);
        JMenuItem removedMenu = chatMenus.remove(tcpConnection);

        chatMenu.removeMenuItem(removedMenu);
        removedWindow.setVisible(false);
        removedWindow.dispose();
    }

    public void receivePublic(MpChatMessage message) {
        publicChatWindow.receiveMessage(message);
    }
    public void receivePrivate(TcpConnection tcpConnection, MpChatMessage message) {
        chatWindows.get(tcpConnection).receiveMessage(message);
    }

    public void openChatWindow(InetAddress address) {

    }

    @Override
    public void onPeersChanged(List<User> list) {

    }
}
