package pitiupi.chat.GUI;

import pitiupi.models.Menus.MulticastMenus.MulticastActionMenu;;

public class ChatMulticastMenu extends MulticastActionMenu {
    ChatWindow chatWindow;

    public ChatMulticastMenu(ChatWindow chatWindow) {
        this.chatWindow = chatWindow;
    }

    @Override
    public String getText() {
        return "Show Chat Window";
    }

    @Override
    public void onClick() {
        chatWindow.setVisible(true);
    }
}
