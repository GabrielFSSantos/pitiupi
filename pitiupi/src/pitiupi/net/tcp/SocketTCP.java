package pitiupi.net.tcp;

import pitiupi.GUI.MainWindow;
import pitiupi.net.Message;
import pitiupi.plugin.Plugin;
import pitiupi.plugin.PrivateConnectionPlugin;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gerencia a comunicação TCP da aplicação.
 *
 * <p>Mantém conexões persistentes com os peers e reutiliza essas conexões
 * para o envio de mensagens. As mensagens recebidas são encaminhadas aos
 * plugins, que são responsáveis por interpretá-las.</p>
 *
 * <p>Novas conexões são aceitas pelo servidor e cada conexão possui uma
 * tarefa própria para receber mensagens, permitindo que múltiplas conexões
 * sejam mantidas simultaneamente.</p>
 *
 * <p>As conexões são associadas aos endereços dos peers e podem ser
 * encerradas quando um peer deixa de estar ativo.</p>
 *
 * @author Gustavo
 */
public class SocketTCP extends Thread {
    private final Logger logger;

    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();

    private final ConnectionManager connectionManager;

    private ServerSocket serverSocket;
    private InetAddress address;

    private final MainWindow main;

    private volatile boolean running = true;

    /**
     * Cria o servidor TCP da aplicação.
     *
     * <p>O servidor utiliza a porta definida pela aplicação e inicia sem
     * estabelecer conexões com os peers. As conexões são criadas conforme
     * necessário durante o envio ou recebidas de outros peers.</p>
     *
     * @param main janela principal da aplicação.
     */
    public SocketTCP(MainWindow main) {
        this.main = main;
        logger = Logger.getLogger(getClass().getName());
        connectionManager = new ConnectionManager();
        try {
            serverSocket = new ServerSocket(main.getPort());
            address = InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
        } catch (IOException ex) {
            System.out.println("There is no socket connection. Sorry.");
            System.out.println(ex.toString());
        }
    }

    public void send(byte[] msg, InetAddress destinationAddress) {
        try {
            TcpConnection connection = getPublicConnection(destinationAddress, main.getIdentifier(), main.getIdentifier());
            if (connection == null) {
                System.out.println("Could not establish TCP connection.");
                return;
            }
            connection.send(msg);
        }  catch (Exception ex) {
            System.out.println("Could not send TCP message.");
            System.out.println(ex.toString());
        }
    }

    public TcpConnection createPrivateConnection(InetAddress destinationAddress, String senderId, String receiverId) {
        return establishOutgoingConnection(destinationAddress, senderId, receiverId, true);
    }

    private synchronized TcpConnection getPublicConnection(InetAddress destinationAddress, String senderId, String receiverId) {
        TcpConnection connection;

        connection = connectionManager.getPublicConnection(destinationAddress);
        if (connection != null && !connection.isClosed()) return connection;

        connection = establishOutgoingConnection(destinationAddress, senderId, receiverId, false);

        return connection;
    }

    /**
     * Aguarda e aceita novas conexões TCP.
     *
     * <p>Cada conexão aceita é associada ao endereço do peer e registrada para
     * que possa ser utilizada tanto para recepção quanto para envio de
     * mensagens.</p>
     */
    private void receive() {
        while (running) {

            try {
                Socket socket = serverSocket.accept();
                acceptIncomingConnection(socket);

            } catch (IOException ex) {
                if (!running) {
                    break;
                }

                System.out.println("Error accepting TCP connection.");
                System.out.println(ex.toString());
            }
        }
    }

    private TcpConnection establishOutgoingConnection(InetAddress destinationAddress, String sender, String receiver, boolean isPrivate) {
        if (address.equals(destinationAddress)) {
            System.out.println("Failed to get socket.");
            throw new IllegalArgumentException("Connections to the local host are forbidden.");
        }

        TcpConnection connection = ConnectionFactory.createConnection(destinationAddress, main.getPort(), sender, receiver, isPrivate);
        if (connection == null) return null;

        saveConnection(connection);

        connectionExecutor.submit(() -> {
            startReceiver(connection, false);
        });

        return connection;
    }

    private void acceptIncomingConnection(Socket socket) {
        connectionExecutor.submit(() -> {
            TcpConnection connection = ConnectionFactory.receiveConnection(socket);
            if (connection == null) return;

            saveConnection(connection);
            startReceiver(connection, true);
        });
    }

    private void saveConnection(TcpConnection connection) {
        if (connection.isPrivate()) {
            connectionManager.addPrivateConnection(connection.getAddress(), connection);
        }
        else  {
            connectionManager.addPublicConnection(connection.getAddress(), connection);
        }
    }

    private void startReceiver(TcpConnection connection, boolean notify) {
        try {
            if (!connection.isPrivate()) {
                publicConnectionReceiver(connection);
                return;
            }

            if (connection.getReceiver().equals(main.getIdentifier())) {
                systemConnectionReceiver(connection);
                return;
            }

            Plugin receiver = findConnectionReceiver(connection);
            if (receiver instanceof PrivateConnectionPlugin plugin) {
                if (notify) {
                    plugin.receivePrivateConnection(connection);
                }
                privateConnectionReceiver(connection, plugin);
            }
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private Plugin findConnectionReceiver(TcpConnection connection) {
        Plugin receiver = null;
        for(Plugin plugin : main.getPlugins()) {
            if (plugin.getIdentifier().equals(connection.getReceiver())) {
                receiver = plugin;
                break;
            }
        }

        if (receiver == null) {
            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.RECEIVER_NOT_FOUND,
                    "This peer does not have the plugin: " + connection.getReceiver()
            );
        }
        return receiver;
    }

    private void publicConnectionReceiver(TcpConnection connection) {
        while (running) {
            try {
                byte[] bytes = connection.receive();
                try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    ObjectInputStream ois = new ObjectInputStream(bis)) {

                    Object object = ois.readObject();
                    if (object instanceof Message message) {
                        SysInfoMessage sysMessage = SysInfoMessage.parseSystemInfo(message);

                        if (sysMessage == null) {
                            for (Plugin plugin : main.getPlugins()) {
                                plugin.receiveMessage(message);
                            }
                            continue;
                        }

                        switch (sysMessage.getType()) {
                            case CONNECTION_CLOSED, INTERNAL_ERROR -> {
                                connectionManager.closeConnection(connection);
                                return;
                            }
                            case MESSAGE_REJECTED -> {
                                logger.log(Level.SEVERE, "Somehow sent something that was not an Message");
                            }
                            default -> {}
                        }
                    }
                    else {
                        SysInfoMessage.sendSystemInfo(
                            connection,
                            SysInfoType.MESSAGE_REJECTED,
                            "Public messages must be instances of Message"
                        );
                    }
                }
                catch (ClassNotFoundException ex) {
                    SysInfoMessage.sendSystemInfo(
                            connection,
                            SysInfoType.INTERNAL_ERROR,
                            "class Message not found."
                    );
                    logger.log(Level.SEVERE, null, ex);
                    connectionManager.closeConnection(connection);
                }
            }
            catch (SocketTimeoutException ex) {
                if (connection.hasTimedOut()) {
                    connectionManager.closeConnection(connection, "Connection timeout");
                    return;
                }
            }
            catch (IOException ex) {
                connectionManager.closeConnection(connection);
            }
        }
    }

    private void privateConnectionReceiver(TcpConnection connection, Plugin plugin) {
        if (plugin instanceof PrivateConnectionPlugin privatePlugin) {
            while (running) {
                try {
                    byte[] message = connection.receive();
                    SysInfoMessage sysMessage = SysInfoMessage.parseSystemInfo(message);

                    if (sysMessage == null) {
                        privatePlugin.receiveBytes(message);
                        continue;
                    }

                    switch (sysMessage.getType()) {
                        case CONNECTION_CLOSED, INTERNAL_ERROR -> {
                            privatePlugin.onSocketClosed(connection);
                            connectionManager.closeConnection(connection);
                        }
                        default -> {
                            continue;
                        }
                    }
                }
                catch (SocketTimeoutException ex) {
                    if (connection.hasTimedOut()) {
                        connectionManager.closeConnection(connection, "Connection timeout");
                        return;
                    }
                }
                catch (IOException ex) {
                    connectionManager.closeConnection(connection);
                }
            }
        }
    }

    private void systemConnectionReceiver(TcpConnection connection) throws IOException {
        while (running) {
            try {
                byte[] message = connection.receive();
                try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {

                    SysInfoMessage sysMessage = SysInfoMessage.parseSystemInfo(message);
                    if (sysMessage != null) continue;

                    //TODO: Ainda não existem mensagens trocadas entre o sistema.
                }

            } catch (SocketTimeoutException ex) {
                if (connection.hasTimedOut()) return;
            }
        }
    }

    public void disconnect(TcpConnection connection, String description) {
        connectionManager.closeConnection(
                connection,
                Objects.requireNonNullElse(description, "No reason provided")
        );
    }

    @Override
    public void run() {
        receive();
    }

    public void close() {
        running = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        connectionManager.closeAllConnections("The peer must have closed the application or changed ports");

        connectionExecutor.shutdownNow();
    }
}