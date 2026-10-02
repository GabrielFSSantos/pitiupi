package GUI;

import pitiupi.models.Menus.MenuCategory;
import pitiupi.models.Menus.MulticastMenus.MulticastSubMenu;
import plugin.MultiProtocolChat;

import javax.swing.*;
import java.awt.*;

public class ChatMenu extends MulticastSubMenu {
    private final MultiProtocolChat multiProtocolChat;

    public ChatMenu(MultiProtocolChat multiProtocolChat) {
        this.multiProtocolChat = multiProtocolChat;
        this.setText("MP-Chat");

        JMenuItem startPrivateChat = new JMenuItem("start Private Chat");
        startPrivateChat.addActionListener(e -> showPrivateChatDialog());
        this.add(startPrivateChat);
    }

    public void newMenuItem(JMenuItem newMenuItem, ChatWindow chatWindow) {
        newMenuItem.addActionListener(e -> chatWindow.setVisible(true));

        this.add(newMenuItem);
    }

    public void removeMenuItem(JMenuItem removeMenuItem) {
        this.remove(removeMenuItem);
    }

    private void showPrivateChatDialog() {

    }

    @Override
    public String getText() {
        return "MP-Chat";
    }
}
