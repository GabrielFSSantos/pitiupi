/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.chat.GUI;

import pitiupi.models.Menus.MulticastMenus.MulticastMenu;
import pitiupi.models.Menus.MulticastMenus.MulticastSubMenu;

import java.util.List;

/**
 *
 * @author flavio
 */
public class ChatMenu extends MulticastSubMenu {
    ChatMulticastMenu chatMulticastMenu;

    public ChatMenu(ChatWindow chatWindow) {
        this.chatMulticastMenu = new ChatMulticastMenu(chatWindow);
    }

    @Override
    public String getText() {
        return "Chat";
    }

    @Override
    public List<MulticastMenu> getMenus() {
        return List.of(chatMulticastMenu);
    }
}
