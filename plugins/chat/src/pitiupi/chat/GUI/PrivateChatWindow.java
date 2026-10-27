package pitiupi.chat.GUI;

import pitiupi.chat.net.LegacyMpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivateChatWindow extends JFrame {

    private final ConnectionManager connectionManager;
    private final TcpConnection connection;
    private final JTextArea receivedMessagesArea;
    private final JTextField messageField;

    public PrivateChatWindow(ConnectionManager connectionManager, TcpConnection connection) {
        this.connectionManager = connectionManager;
        this.connection = connection;

        setTitle("Chat privado (TCP)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);

        JScrollPane messagesScrollPane = new JScrollPane(receivedMessagesArea);
        messagesScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Mensagens"));
        mainPanel.add(messagesScrollPane, BorderLayout.CENTER);

        messageField = new JTextField();
        JButton tcpButton = new JButton("Enviar TCP");
        tcpButton.addActionListener(e -> sendTCP());

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(tcpButton, BorderLayout.EAST);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    public void sendChatLine(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        messageField.setText(text);
        sendTCP();
    }

    private void sendTCP() {
        try {
            LegacyMpChatMessage message = new LegacyMpChatMessage(
                    connectionManager.getUserName(),
                    pitiupi.presentation.shell.LocalNetworkInfo.bestLocalIpv4(),
                    "TCP privado",
                    messageField.getText()
            );
            connection.send(message.toByteArray());
            receiveMessage(message);
            messageField.setText("");
        } catch (Exception ex) {
            Logger.getLogger(PrivateChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveMessage(LegacyMpChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            String texto = String.format(
                    "[%s] %s (%s): %s%n",
                    message.getTipoMensagem(),
                    message.getNome(),
                    message.getIp(),
                    message.getMessage()
            );
            receivedMessagesArea.append(texto);
            receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());
        });
    }
}
