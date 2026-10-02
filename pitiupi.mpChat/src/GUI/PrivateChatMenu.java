package GUI;

import control.ChatController;
import pitiupi.models.Menus.UnicastMenus.UnicastActionMenu;
import pitiupi.models.User;
import plugin.MultiProtocolChat;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;

public class PrivateChatMenu extends UnicastActionMenu {
    PrivateChatWindow privateChatWindow;
    private final MultiProtocolChat multiProtocolChat;
    private final ChatController chatController;

    public PrivateChatMenu(PrivateChatWindow PrivateChatWindow, MultiProtocolChat multiProtocolChat, ChatController chatController) {
        this.privateChatWindow = PrivateChatWindow;
        this.multiProtocolChat = multiProtocolChat;
        this.chatController = chatController;
    }

    @Override
    public void onClick(User user) {
        if (multiProtocolChat.openConnection(user.getAddress())
        else errorLabel.setVisible(true);

        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(privateChatWindow));
        dialog.setVisible(true);
    }

    @Override
    public String getText() {
        return "start Private Chat";
    }
}
