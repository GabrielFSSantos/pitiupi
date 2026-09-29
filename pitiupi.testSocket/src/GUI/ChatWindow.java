package GUI;

import net.TestChatMessage;
import pitiupi.GUI.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class ChatWindow extends JFrame {

    protected final MainWindow mainWindow;

    private JTextArea receivedMessagesArea;
    protected JTextField messageField;

    public ChatWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        initComponents();
    }

    private void initComponents() {
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
        messageField = new JTextField();

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        bottomPanel.add(messageField, BorderLayout.CENTER);

        initButtons(bottomPanel);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    protected abstract void initButtons(JPanel bottomPanel);

    protected TestChatMessage createMessage(String tipoMensagem) {
        try {
            String nome = mainWindow.getUserName();
            String ip = InetAddress.getLocalHost().getHostAddress();
            String message = messageField.getText();

            return new TestChatMessage(nome, ip, tipoMensagem, message);

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void receiveMessage(TestChatMessage msg) {
        SwingUtilities.invokeLater(() -> {
            String mensagem = String.format(
                    "[%s] %s (%s): %s%n",
                    msg.getTipoMensagem(),
                    msg.getNome(),
                    msg.getIp(),
                    msg.getMessage()
            );

            receivedMessagesArea.append(mensagem);

            receivedMessagesArea.setCaretPosition(receivedMessagesArea.getDocument().getLength());
        });
    }
}