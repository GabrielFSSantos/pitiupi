package GUI;

import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.net.tcp.TcpConnection;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivateChatWindow extends ChatWindow {

    private final TcpConnection connection;

    public PrivateChatWindow(ConnectionManager ConnectionManager, TcpConnection connection) {
        super(ConnectionManager);
	this.connection = connection;
    }

    @Override
    protected void initButtons(JPanel bottomPanel) {
        JButton tcpButton = new JButton("TCP");

        tcpButton.addActionListener(e -> sendTCP());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        buttonsPanel.add(tcpButton);

        bottomPanel.add(buttonsPanel, BorderLayout.EAST);
    }

    private void sendTCP() {
        try {
            MpChatMessage msg = createMessage("TCP Private");
            connection.send(msg.toByteArray());

        } catch (Exception ex) {
            Logger.getLogger(PrivateChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
