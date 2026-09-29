package GUI;

import net.TestChatMessage;
import pitiupi.GUI.MainWindow;
import pitiupi.control.ConnectionManager;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PublicChatWindow extends ChatWindow {

    private JTextField ipField;

    public PublicChatWindow(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected void initButtons(JPanel bottomPanel) {
        initIpField(bottomPanel);
        initCommunicationButtons(bottomPanel);
    }

    private void initIpField(JPanel bottomPanel) {
        ipField = new JTextField();
        ipField.setPreferredSize(new Dimension(130, 30));

        JPanel ipPanel = new JPanel(new BorderLayout(5, 5));

        ipPanel.add(new JLabel("IP destino:"), BorderLayout.WEST);

        ipPanel.add(ipField, BorderLayout.CENTER);

        bottomPanel.add(ipPanel, BorderLayout.WEST);
    }

    private void initCommunicationButtons(JPanel bottomPanel) {
        JButton tcpButton = new JButton("TCP");
        JButton udpButton = new JButton("UDP");
        JButton multicastButton = new JButton("Multicast");

        tcpButton.addActionListener(e -> sendTCP());
        udpButton.addActionListener(e -> sendUDP());
        multicastButton.addActionListener(e -> sendMulticast());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        buttonsPanel.add(tcpButton);
        buttonsPanel.add(udpButton);
        buttonsPanel.add(multicastButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    private void sendTCP() {
        try {
            TestChatMessage msg = createMessage("TCP Unicast");

            InetAddress ip = InetAddress.getByName(ipField.getText());

            connectionManager.sendTcp(msg, ip);

        } catch (Exception ex) {
            Logger.getLogger(PublicChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendUDP() {
        try {
            TestChatMessage msg = createMessage("UDP Unicast");

            InetAddress ip = InetAddress.getByName(ipField.getText());

            connectionManager.sendUdpUnicast(msg, ip);

        } catch (Exception ex) {
            Logger.getLogger(PublicChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendMulticast() {
        try {
            TestChatMessage msg = createMessage("UDP Multicast");

            connectionManager.sendUdpMulticast(msg);

        } catch (Exception ex) {
            Logger.getLogger(PublicChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}