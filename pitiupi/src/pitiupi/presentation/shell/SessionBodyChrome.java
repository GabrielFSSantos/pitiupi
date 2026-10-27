package pitiupi.presentation.shell;

import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;

/**
 * Envolve o corpo da sessão com separador leve abaixo da faixa de abas.
 */
public final class SessionBodyChrome extends JPanel {

    public SessionBodyChrome(JPanel bodyHost) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 0, 0, 0, ShellUi.shellEdgeColor()),
                new EmptyBorder(2, 2, 2, 2)
        ));
        add(bodyHost, BorderLayout.CENTER);
    }
}
