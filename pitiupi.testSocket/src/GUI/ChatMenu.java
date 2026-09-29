package GUI;

import plugin.TestChat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ChatMenu extends JMenu {
    private final TestChat testChat;

    public ChatMenu(TestChat testChat) {
        this.testChat = testChat;
        this.setText("MP-Chat");

        JMenuItem startPrivateChat = new JMenuItem("start Private Chat");
        startPrivateChat.addActionListener(_ -> showPrivateChatDialog());
        this.add(startPrivateChat);
    }

    public void newMenuItem(JMenuItem newMenuItem, ChatWindow chatWindow) {
        newMenuItem.addActionListener(_ -> chatWindow.setVisible(true));

        this.add(newMenuItem);
    }

    public void removeMenuItem(JMenuItem removeMenuItem) {
        this.remove(removeMenuItem);
    }

    private void showPrivateChatDialog() {
        JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Novo chat privado",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        JTextField ipField = new JTextField();

        JButton startButton = new JButton("Iniciar");
        JButton cancelButton = new JButton("Cancelar");

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JPanel ipPanel = new JPanel(new BorderLayout(5, 5));
        ipPanel.add(new JLabel("IP:"), BorderLayout.WEST);
        ipPanel.add(ipField, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(startButton);

        JLabel errorLabel = new JLabel("Não foi possível estabelecer a conexão.");
        errorLabel.setForeground(Color.RED);
        errorLabel.setVisible(false);

        contentPanel.add(errorLabel, BorderLayout.NORTH);
        contentPanel.add(ipPanel, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dialog.dispose());

        startButton.addActionListener(e -> {
            String ip = ipField.getText();

            if (testChat.openConnection(ip)) dialog.dispose();
            else errorLabel.setVisible(true);

        });

        dialog.setContentPane(contentPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }
}
