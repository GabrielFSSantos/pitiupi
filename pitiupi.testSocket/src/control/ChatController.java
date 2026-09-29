package control;

import GUI.ChatMenu;
import GUI.ChatWindow;
import GUI.PrivateChatWindow;
import GUI.PublicChatWindow;
import net.TestChatMessage;
import pitiupi.GUI.MainWindow;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.*;
import java.util.HashMap;

public class ChatController {
    MainWindow mainWindow;
    ChatMenu chatMenu;

    PublicChatWindow publicChatWindow;

    private final HashMap<TcpConnection, JMenuItem> chatMenus;
    private final HashMap<TcpConnection, ChatWindow>  chatWindows;


    public ChatController(MainWindow mainWindow, ChatMenu chatMenu) {
        this.mainWindow = mainWindow;
        publicChatWindow = new PublicChatWindow(mainWindow);

        chatMenus = new HashMap<>();
        chatWindows = new HashMap<>();

        chatMenu.newMenuItem(new JMenuItem("Show global chat"), publicChatWindow);
    }

    public void addChatWindow(TcpConnection tcpConnection) {
        if (chatWindows.containsKey(tcpConnection)) return;

        PrivateChatWindow newChat = new PrivateChatWindow(mainWindow, tcpConnection);
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

    public void receivePublic(TestChatMessage message) {
        publicChatWindow.receiveMessage(message);
    }
    public void receivePrivate(TcpConnection tcpConnection, TestChatMessage message) {
        chatWindows.get(tcpConnection).receiveMessage(message);
    }
}
