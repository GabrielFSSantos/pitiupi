package GUI;

import pitiupi.models.Menus.UnicastMenus.UnicastActionMenu;
import pitiupi.models.User;
import plugin.MultiProtocolChat;

import javax.swing.*;

public class UnicastMpChatMenu extends UnicastActionMenu {
    private final MultiProtocolChat multiProtocolChat;

    public UnicastMpChatMenu(MultiProtocolChat multiProtocolChat) {
        this.multiProtocolChat = multiProtocolChat;
    }

    @Override
    public void onClick(User user) {
        multiProtocolChat.openConnection(user.getAddress().toString());
    }

    @Override
    public String getText() {
        return "start Private Chat";
    }
}
