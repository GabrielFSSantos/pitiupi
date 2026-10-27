package pitiupi.presentation.shell;

import pitiupi.GUI.About;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * Barra de menus mínima (apenas Ajuda).
 */
public final class ShellMenuBar extends JMenuBar {

    public ShellMenuBar() {
        JMenu help = new JMenu("Ajuda");
        JMenuItem about = new JMenuItem("Sobre");
        about.addActionListener(e -> new About().setVisible(true));
        help.add(about);
        add(help);
    }
}
