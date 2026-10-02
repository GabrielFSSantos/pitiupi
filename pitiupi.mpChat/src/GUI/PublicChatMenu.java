package GUI;

import pitiupi.models.Menus.MulticastMenus.MulticastActionMenu;

public class PublicChatMenu extends MulticastActionMenu {
    PublicChatWindow publicChatWindow;

    PublicChatMenu(PublicChatWindow publicChatWindow) {
        this.publicChatWindow = publicChatWindow;
    }

    @Override
    public void onClick() {
        publicChatWindow.setVisible(true);
    }

    @Override
    public String getText() {
        return "";
    }
}
