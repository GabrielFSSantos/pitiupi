package GUI;

import net.MpChatMessage;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.plugin.Pitiupi;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivateChatWindow extends JFrame {

    private final Pitiupi pitiupi;
    private final TcpConnection connection;

    private final JTextArea receivedMessagesArea;
    private final JTextField messageField;

    public PrivateChatWindow(
            Pitiupi pitiupi,
            TcpConnection connection
    ) {
        this.pitiupi = pitiupi;
        this.connection = connection;

        setTitle("Chat privado");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);

        JScrollPane messagesScrollPane =
                new JScrollPane(receivedMessagesArea);

        messagesScrollPane.setBorder(
                BorderFactory.createTitledBorder("Mensagens")
        );

        mainPanel.add(
                messagesScrollPane,
                BorderLayout.CENTER
        );

        JPanel bottomPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        messageField = new JTextField();

        JButton tcpButton = new JButton("TCP");
        tcpButton.addActionListener(e -> sendTCP());

        bottomPanel.add(
                messageField,
                BorderLayout.CENTER
        );

        bottomPanel.add(
                tcpButton,
                BorderLayout.EAST
        );

        mainPanel.add(
                bottomPanel,
                BorderLayout.SOUTH
        );

        setContentPane(mainPanel);
    }

    private void sendTCP() {
        try {
            String nome = pitiupi.getUserName();

            MpChatMessage message = new MpChatMessage(
                    nome,
                    InetAddress.getLocalHost().getHostAddress(),
                    "TCP Private",
                    messageField.getText()
            );

            connection.send(message.toByteArray());
            receiveMessage(message);

            messageField.setText("");

        } catch (Exception ex) {
            Logger.getLogger(
                    PrivateChatWindow.class.getName()
            ).log(Level.SEVERE, null, ex);
        }
    }

    public void receiveMessage(MpChatMessage message) {
        SwingUtilities.invokeLater(() -> {
            String texto = String.format(
                    "[%s] %s (%s): %s%n",
                    message.getTipoMensagem(),
                    message.getNome(),
                    message.getIp(),
                    message.getMessage()
            );

            receivedMessagesArea.append(texto);

            receivedMessagesArea.setCaretPosition(
                    receivedMessagesArea.getDocument().getLength()
            );
        });
    }
}