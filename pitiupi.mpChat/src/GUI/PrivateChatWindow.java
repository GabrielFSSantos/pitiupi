package GUI;

import net.MpChatMessage;
import pitiupi.control.ConnectionManager;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.plugin.Pitiupi;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrivateChatWindow extends JFrame {

    private final Pitiupi pitiupi;

    private final Map<TcpConnection, PrivateChatPanel> chats;

    private final JPanel chatPanel;
    private final CardLayout cardLayout;

    private final DefaultListModel<TcpConnection> connectionListModel;
    private final JList<TcpConnection> connectionList;

    public PrivateChatWindow(Pitiupi pitiupi) {
        this.pitiupi = pitiupi;
        this.chats = new HashMap<>();

        setTitle("Chats privados");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        connectionListModel = new DefaultListModel<>();
        connectionList = new JList<>(connectionListModel);

        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionList.setPreferredSize(new Dimension(180, 0));

        connectionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                TcpConnection connection = connectionList.getSelectedValue();

                if (connection != null) {
                    showChat(connection);
                }
            }
        });

        JScrollPane connectionScrollPane = new JScrollPane(connectionList);
        connectionScrollPane.setBorder(
                BorderFactory.createTitledBorder("Usuários")
        );

        mainPanel.add(connectionScrollPane, BorderLayout.WEST);

        cardLayout = new CardLayout();
        chatPanel = new JPanel(cardLayout);

        mainPanel.add(chatPanel, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    public void addConnection(TcpConnection connection) {
        if (chats.containsKey(connection)) {
            return;
        }

        PrivateChatPanel panel = new PrivateChatPanel(connection);

        chats.put(connection, panel);
        connectionListModel.addElement(connection);

        String cardName = getCardName(connection);
        chatPanel.add(panel, cardName);

        if (connectionList.getSelectedValue() == null) {
            connectionList.setSelectedValue(connection, true);
        }
    }

    public void removeConnection(TcpConnection connection) {
        PrivateChatPanel panel = chats.remove(connection);

        if (panel == null) {
            return;
        }

        chatPanel.remove(panel);
        connectionListModel.removeElement(connection);

        if (!connectionListModel.isEmpty()) {
            connectionList.setSelectedIndex(0);
        }
    }

    public void receiveMessage(TcpConnection connection, MpChatMessage message) {
        PrivateChatPanel panel = chats.get(connection);

        if (panel == null) {
            return;
        }

        panel.receiveMessage(message);
    }

    private void showChat(TcpConnection connection) {
        cardLayout.show(chatPanel, getCardName(connection));
    }

    private String getCardName(TcpConnection connection) {
        return Integer.toHexString(System.identityHashCode(connection));
    }

    private class PrivateChatPanel extends JPanel {

        private final TcpConnection connection;

        private final JTextArea receivedMessagesArea;
        private final JTextField messageField;

        public PrivateChatPanel(TcpConnection connection) {
            this.connection = connection;

            setLayout(new BorderLayout(10, 10));

            receivedMessagesArea = new JTextArea();
            receivedMessagesArea.setEditable(false);
            receivedMessagesArea.setLineWrap(true);
            receivedMessagesArea.setWrapStyleWord(true);

            JScrollPane messagesScrollPane =
                    new JScrollPane(receivedMessagesArea);

            messagesScrollPane.setBorder(
                    BorderFactory.createTitledBorder("Mensagens")
            );

            add(messagesScrollPane, BorderLayout.CENTER);

            JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

            messageField = new JTextField();

            JButton tcpButton = new JButton("TCP");
            tcpButton.addActionListener(e -> sendTCP());

            bottomPanel.add(messageField, BorderLayout.CENTER);
            bottomPanel.add(tcpButton, BorderLayout.EAST);

            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void sendTCP() {
            try {
                String nome = pitiupi.getUserName();

                MpChatMessage message = new MpChatMessage(
                        nome,
                        InetAddress.getLocalHost().toString(),
                        "TCP Private",
                        messageField.getText()
                );

                connection.send(message.toByteArray());

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
}
