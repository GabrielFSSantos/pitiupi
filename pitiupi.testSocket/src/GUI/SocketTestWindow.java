package GUI;

import net.TestMessage;
import pitiupi.GUI.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocketTestWindow extends JFrame {
    private MainWindow mainWindow;
    private JTextArea receivedMessagesArea;

    private JTextField ipField;
    private JTextField messageField;

    private JButton tcpButton;
    private JButton udpButton;
    private JButton multicastButton;

    public SocketTestWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initComponents();
    }

    public void initComponents() {
        setTitle("Socket Test");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        initReceivedMessagesPanel(mainPanel);
        initBottomPanel(mainPanel);

        setContentPane(mainPanel);
    }

    private void initReceivedMessagesPanel(JPanel mainPanel) {
        receivedMessagesArea = new JTextArea();
        receivedMessagesArea.setEditable(false);
        receivedMessagesArea.setLineWrap(true);
        receivedMessagesArea.setWrapStyleWord(true);

        JScrollPane messagesScrollPane = new JScrollPane(receivedMessagesArea);

        messagesScrollPane.setBorder(BorderFactory.createTitledBorder("Mensagens recebidas"));

        mainPanel.add(messagesScrollPane, BorderLayout.CENTER);
    }

    private void initBottomPanel(JPanel mainPanel) {
        ipField = new JTextField();
        ipField.setPreferredSize(new Dimension(130, 30));

        JPanel ipPanel = new JPanel(new BorderLayout(5, 5));
        ipPanel.add(new JLabel("IP destino:"), BorderLayout.WEST);
        ipPanel.add(ipField, BorderLayout.CENTER);

        messageField = new JTextField();

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        bottomPanel.add(ipPanel, BorderLayout.WEST);
        bottomPanel.add(messageField, BorderLayout.CENTER);
        initButtons(bottomPanel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initButtons(JPanel bottomPanel) {
        tcpButton = new JButton("TCP");
        udpButton = new JButton("UDP");
        multicastButton = new JButton("Multicast");

        tcpButton.addActionListener(e -> sendTCP());
        udpButton.addActionListener(e -> sendUDP());
        multicastButton.addActionListener(e -> sendMulticast());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        buttonsPanel.add(tcpButton);
        buttonsPanel.add(udpButton);
        buttonsPanel.add(multicastButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    public void sendTCP() {
        try {
            TestMessage msg = createMessage("TCP Unicast");
            InetAddress ip = InetAddress.getByName(ipField.getText());
            mainWindow.getSocketTCP().send(msg.toByteArray(), ip);
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    public void sendUDP() {
        try {
            TestMessage msg = createMessage("UDP Unicast");
            InetAddress ip = InetAddress.getByName(ipField.getText());
            mainWindow.getSocketUDP().sendUnicast(msg.toByteArray(), ip);
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendMulticast() {
        try {
            TestMessage msg = createMessage("UDP Multicast");
            mainWindow.getSocketUDP().sendMulticast(msg.toByteArray());
        } catch (Exception ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private TestMessage createMessage(String tipoMensagem) {
        try {
            String nome = mainWindow.getUserName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            String message = messageField.getText();
            return new TestMessage(nome, ip, tipoMensagem, message);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(TestMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String mensagem = String.format(
                    "[%s] %s (%s): %s%n",
                    msg.getTipoMensagem(),
                    msg.getNome(),
                    msg.getIp(),
                    msg.getMessage()
            );

            receivedMessagesArea.append(mensagem);

            receivedMessagesArea.setCaretPosition(
                    receivedMessagesArea.getDocument().getLength()
            );
        });
    }
}
