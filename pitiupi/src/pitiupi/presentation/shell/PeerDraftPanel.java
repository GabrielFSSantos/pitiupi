package pitiupi.presentation.shell;

import pitiupi.presentation.shell.ui.ShellUi;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;

/**
 * Entrada de IP inline para aba rascunho (Nova conversa).
 */
public final class PeerDraftPanel extends JPanel {

    private final JTextField ipField = new JTextField(24);
    private final Consumer<String> onCommit;

    public PeerDraftPanel(String title, String hint, Consumer<String> onCommit) {
        this.onCommit = onCommit;
        setLayout(new BorderLayout(8, 8));
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(new JLabel(title), BorderLayout.NORTH);
        center.add(ShellUi.mutedLabel(hint), BorderLayout.CENTER);
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        row.add(new JLabel("IP do peer:"));
        row.add(ipField);
        JButton open = new JButton("Abrir conversa");
        open.addActionListener(e -> commit());
        row.add(open);
        center.add(row, BorderLayout.SOUTH);
        add(center, BorderLayout.NORTH);
        ipField.addActionListener(e -> commit());
    }

    public void focusIpField() {
        ipField.requestFocusInWindow();
    }

    public void clearIpField() {
        ipField.setText("");
    }

    private void commit() {
        String ip = ipField.getText();
        if (ip == null || ip.isBlank()) {
            return;
        }
        if (onCommit != null) {
            onCommit.accept(ip.trim());
        }
    }
}
