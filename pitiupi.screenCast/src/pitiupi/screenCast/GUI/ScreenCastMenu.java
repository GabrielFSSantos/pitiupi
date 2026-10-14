/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.screenCast.GUI;

import java.util.List;

import pitiupi.models.Menus.MulticastMenus.MulticastActionMenu;
import pitiupi.models.Menus.MulticastMenus.MulticastMenu;
import pitiupi.models.Menus.MulticastMenus.MulticastSubMenu;

/**
 *
 * @author flavio
 */
public class ScreenCastMenu extends MulticastSubMenu {

    private final ScreenCastShowMenu showMenu;

    public ScreenCastMenu(ScreenCastWindow screenCastWindow) {
        this.showMenu = new ScreenCastShowMenu(screenCastWindow);
    }

    @Override
    public String getText() {
        return "Screen Cast";
    }

    @Override
    public List<MulticastMenu> getMenus() {
        return List.of(showMenu);
    }

    private static final class ScreenCastShowMenu extends MulticastActionMenu {
        private final ScreenCastWindow screenCastWindow;

        ScreenCastShowMenu(ScreenCastWindow screenCastWindow) {
            this.screenCastWindow = screenCastWindow;
        }

        @Override
        public String getText() {
            return "ScreenCast Window";
        }

        @Override
        public void onClick() {
            screenCastWindow.setVisible(true);
        }
    }
}
