package GUI;

import control.PrivateChatController;
import pitiupi.models.Menus.UnicastMenus.UnicastActionMenu;
import pitiupi.models.User;

public class UnicastMpChatMenu extends UnicastActionMenu {

    private final PrivateChatController controller;

    public UnicastMpChatMenu(
            PrivateChatController controller
    ) {
        this.controller = controller;
    }

    @Override
    public void onClick(User user) {
        controller.openChat(user);
    }

    @Override
    public String getText() {
        return "Start Private Chat";
    }
}