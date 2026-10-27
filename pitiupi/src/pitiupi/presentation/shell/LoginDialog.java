package pitiupi.presentation.shell;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.prefs.Preferences;

/**
 * Login modal centralizado (~30% da área útil do monitor).
 */
public final class LoginDialog extends JDialog {

    private static final Preferences prefs = Preferences.userNodeForPackage(LoginDialog.class);
    private static final String PREF_NOME = "lastUserName";
    private static final String PREF_PORTA = "lastPort";

    private final JTextField nameField = new JTextField(24);
    private final JSpinner portField = new JSpinner();
    private boolean accepted;

    public LoginDialog() {
        super((java.awt.Frame) null, "Pitiupi", true);
        setLocationByPlatform(false);
        UiGeometry.installCenterOnShow(this);
        buildUi();
        applyPreferredSize();
    }

    private void buildUi() {
        String nomeSalvo = prefs.get(PREF_NOME, System.getProperty("user.name"));
        int portaSalva = prefs.getInt(PREF_PORTA, 5000);
        portField.setModel(new javax.swing.SpinnerNumberModel(portaSalva, 1, 65535, 1));
        nameField.setText(nomeSalvo);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 24, 8, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Seu nome na rede:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        form.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Porta:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        form.add(portField, gbc);

        JButton enterButton = new JButton("Entrar");
        JButton cancelButton = new JButton("Cancelar");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setBorder(new EmptyBorder(8, 16, 16, 16));
        buttons.add(enterButton);
        buttons.add(cancelButton);

        enterButton.addActionListener(e -> onEnter());
        cancelButton.addActionListener(e -> System.exit(0));
        getRootPane().setDefaultButton(enterButton);

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private void applyPreferredSize() {
        pack();
        java.awt.Dimension fraction = UiGeometry.fractionOfScreen(0.3, 0.3);
        int width = Math.max(fraction.width, getWidth());
        int height = Math.max(240, getHeight());
        setSize(width, height);
    }

    private void onEnter() {
        try {
            portField.commitEdit();
        } catch (java.text.ParseException ex) {
            return;
        }
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            return;
        }
        int port = (Integer) portField.getValue();
        if (port <= 0) {
            return;
        }
        prefs.put(PREF_NOME, name);
        prefs.putInt(PREF_PORTA, port);
        accepted = true;
        setVisible(false);
        dispose();
    }

    public boolean showLogin() {
        applyPreferredSize();
        validate();
        UiGeometry.centerOnScreen(this);
        setVisible(true);
        SwingUtilities.invokeLater(() -> UiGeometry.centerOnScreen(this));
        return accepted;
    }

    public String getUserName() {
        return nameField.getText().trim();
    }

    public int getPort() {
        return (Integer) portField.getValue();
    }
}
