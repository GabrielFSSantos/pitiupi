package GUI;

import net.MpChatMessage;
import pitiupi.control.ConnectionManager;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicChatWindow extends JFrame {
    private final ConnectionManager connectionManager;

    private JTextPane receivedMessagesArea;
    private JTextField messageField;
    private JTextField ipField;

    public PublicChatWindow(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;

        initComponents();
    }

    private void initComponents() {
        setTitle("Public Chat");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        initReceivedMessagesPanel(mainPanel);
        initBottomPanel(mainPanel);

        setContentPane(mainPanel);
    }

    private void initReceivedMessagesPanel(JPanel mainPanel) {
        receivedMessagesArea = new JTextPane();

        receivedMessagesArea.setEditable(false);

        JScrollPane messagesScrollPane = new JScrollPane(receivedMessagesArea);

        messagesScrollPane.setBorder(
                BorderFactory.createTitledBorder("Mensagens recebidas")
        );

        mainPanel.add(messagesScrollPane, BorderLayout.CENTER);
    }

    private void initBottomPanel(JPanel mainPanel) {
        messageField = new JTextField();

        JPanel bottomPanel = new JPanel(
                new BorderLayout(10, 10)
        );

        bottomPanel.add(messageField, BorderLayout.CENTER);

        initIpField(bottomPanel);
        initCommunicationButtons(bottomPanel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initIpField(JPanel bottomPanel) {
        ipField = new JTextField();
        ipField.setPreferredSize(new Dimension(130, 30));

        JPanel ipPanel = new JPanel(
                new BorderLayout(5, 5)
        );

        ipPanel.add(
                new JLabel("IP destino:"),
                BorderLayout.WEST
        );

        ipPanel.add(
                ipField,
                BorderLayout.CENTER
        );

        bottomPanel.add(ipPanel, BorderLayout.WEST);
    }

    private void initCommunicationButtons(JPanel bottomPanel) {
        JButton tcpButton = new JButton("TCP");
        JButton udpButton = new JButton("UDP");
        JButton multicastButton = new JButton("Multicast");

        tcpButton.addActionListener(e -> sendTCP());
        udpButton.addActionListener(e -> sendUDP());
        multicastButton.addActionListener(e -> sendMulticast());

        JPanel buttonsPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 5, 0)
        );

        buttonsPanel.add(tcpButton);
        buttonsPanel.add(udpButton);
        buttonsPanel.add(multicastButton);

        bottomPanel.add(
                buttonsPanel,
                BorderLayout.EAST
        );
    }

    private MpChatMessage createMessage(String tipoMensagem) {
        try {
            String nome = connectionManager.getUserName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            String message = messageField.getText();

            return new MpChatMessage(
                    nome,
                    ip,
                    tipoMensagem,
                    message
            );

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void receiveMessage(MpChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String mensagem = String.format(
                    "[%s] %s (%s): %s%n",
                    msg.getTipoMensagem(),
                    msg.getNome(),
                    msg.getIp(),
                    msg.getMessage()
            );

            StyledDocument document = receivedMessagesArea.getStyledDocument();

            SimpleAttributeSet attributes = new SimpleAttributeSet();

            if (msg.getTipoMensagem().equals("TCP Unicast") || msg.getTipoMensagem().equals("UDP Unicast")) {
                StyleConstants.setForeground(attributes, new Color(128, 0, 128));
            }

            try {
                document.insertString(
                        document.getLength(),
                        mensagem,
                        attributes
                );

                receivedMessagesArea.setCaretPosition(
                        document.getLength()
                );

            } catch (BadLocationException ex) {
                Logger.getLogger(PublicChatWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void sendTCP() {
        try {
            MpChatMessage msg = createMessage("TCP Unicast");

            InetAddress ip = InetAddress.getByName(ipField.getText());

            connectionManager.sendTcp(msg, ip);
            receiveMessage(msg);

        } catch (Exception ex) {
            Logger.getLogger(
                    PublicChatWindow.class.getName()
            ).log(Level.SEVERE, null, ex);
        }
    }

    private void sendUDP() {
        try {
            MpChatMessage msg = createMessage("UDP Unicast");

            InetAddress ip = InetAddress.getByName(ipField.getText());

            connectionManager.sendUdpUnicast(msg, ip);
            receiveMessage(msg);

        } catch (Exception ex) {
            Logger.getLogger(
                    PublicChatWindow.class.getName()
            ).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMulticast() {
        try {
            MpChatMessage msg = createMessage("UDP Multicast");

            connectionManager.sendUdpMulticast(msg);

        } catch (Exception ex) {
            Logger.getLogger(
                    PublicChatWindow.class.getName()
            ).log(Level.SEVERE, null, ex);
        }
    }
}