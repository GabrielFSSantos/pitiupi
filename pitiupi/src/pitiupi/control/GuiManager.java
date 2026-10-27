package pitiupi.control;

import pitiupi.presentation.shell.NetworkUsersPanel;
import pitiupi.presentation.shell.ShellHost;
import pitiupi.models.Menus.*;
import pitiupi.models.Menus.MulticastMenus.MulticastActionMenu;
import pitiupi.models.Menus.MulticastMenus.MulticastMenu;
import pitiupi.models.Menus.MulticastMenus.MulticastSubMenu;
import pitiupi.models.Menus.UnicastMenus.UnicastActionMenu;
import pitiupi.models.Menus.UnicastMenus.UnicastMenu;
import pitiupi.models.Menus.UnicastMenus.UnicastSubMenu;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuiManager {
    private final ShellHost shellHost;
    private final NetworkUsersPanel networkUsersPanel;

    private Logger logger;

    public GuiManager(ShellHost shellHost, javax.swing.JMenuBar menuBar, NetworkUsersPanel networkUsersPanel) {
        this.shellHost = shellHost;
        this.networkUsersPanel = networkUsersPanel;
        this.logger = Logger.getLogger(GuiManager.class.getName());
    }

    public void createMenu(MenuCategory category, Menu menu) {
        try {
            JMenuItem newMenu = buildMenu(menu);
            if (menu instanceof MulticastMenu) {
                // Onda D: ações multicast migram para peer actions / abas (Onda E).
            }
            if (menu instanceof UnicastMenu) {
                networkUsersPanel.addUnicastMenu(category, newMenu);
            }
        }
        catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    private JMenuItem buildMenu(Menu menu) {
        if (menu instanceof MulticastSubMenu multicastSubMenu) {
            JMenu newMenu = new JMenu(menu.getText());

            for (Menu child : multicastSubMenu.getMenus()) {
                newMenu.add(buildMenu(child));
            }

            return newMenu;
        }

        if (menu instanceof UnicastSubMenu unicastSubMenu) {
            JMenu newMenu = new JMenu(menu.getText());

            for (Menu child : unicastSubMenu.getMenus()) {
                newMenu.add(buildMenu(child));
            }

            return newMenu;
        }

        if (menu instanceof MulticastActionMenu multicastActionMenu) {
            JMenuItem newMenu = new JMenuItem(menu.getText());
            newMenu.addActionListener(e -> multicastActionMenu.onClick());
            return newMenu;
        }

        if  (menu instanceof UnicastActionMenu unicastActionMenu) {
            JMenuItem newMenu = new JMenuItem(menu.getText());

            networkUsersPanel.initUnicastMenu(newMenu, unicastActionMenu::onClick);
            return newMenu;
        }

        throw new IllegalArgumentException("Invalid menu type");
    }
}
