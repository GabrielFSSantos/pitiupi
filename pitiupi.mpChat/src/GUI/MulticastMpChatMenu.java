package GUI;

import pitiupi.models.Menus.MulticastMenus.MulticastActionMenu;

public class MulticastMpChatMenu extends MulticastActionMenu {
    PublicChatWindow publicChatWindow;

    public MulticastMpChatMenu(PublicChatWindow publicChatWindow) {
        this.publicChatWindow = publicChatWindow;
    }

    @Override
    public void onClick() {
        publicChatWindow.setVisible(true);
    }


    @Override
    public String getText() {
        return "MP-Chat";
    }
}
