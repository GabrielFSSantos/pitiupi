package pitiupi.presentation.shell;

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
        about.addActionListener(e -> new AboutDialog().setVisible(true));
        help.add(about);
        add(help);
    }
}
