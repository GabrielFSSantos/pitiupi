package pitiupi.net;

import pitiupi.GUI.MainWindow;
import pitiupi.control.PeerInfo;
import pitiupi.control.PeerListener;
import pitiupi.plugin.Plugin;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
public class SocketTCP extends Thread implements PeerListener {
    private final ExecutorService connectionExecutor = Executors.newCachedThreadPool();
    private final Map<InetAddress, TcpConnection> connections;
    private ServerSocket serverSocket;

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
        connections = new ConcurrentHashMap<>();
        try {
            serverSocket = new ServerSocket(main.getPort());
        } catch (IOException ex) {
            System.out.println("There is no socket connection. Sorry.");
            System.out.println(ex);
        }
    }

    /**
     * Envia uma mensagem TCP para um peer.
     *
     * <p>Uma conexão existente com o peer é reutilizada. Caso não exista uma
     * conexão válida, uma nova conexão é criada e adicionada ao conjunto de
     * conexões ativas.</p>
     *
     * @param msg mensagem serializada a ser enviada.
     * @param destinationAddress endereço IP do peer destinatário.
     */
    public void send(byte[] msg, InetAddress destinationAddress) {
        try {
            TcpConnection connection;

            synchronized (connections) {
                connection= connections.get(destinationAddress);

                if (connection == null || connection.isClosed()) {
                    Socket socket = new Socket(destinationAddress, main.getPort());

                    connection = new TcpConnection(socket);

                    connections.put(destinationAddress, connection);

                    startReceiver(destinationAddress, connection);
                }
            }

            connection.send(msg);

        } catch (IOException ex) {
            System.out.println("Could not send TCP message.");
            System.out.println(ex);
        }
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

                InetAddress address = socket.getInetAddress();

                TcpConnection connection = new TcpConnection(socket);

                TcpConnection oldConnection = connections.put(address, connection);

                if (oldConnection != null) {
                    oldConnection.close();
                }

                startReceiver(address, connection);

            } catch (IOException ex) {

                if (!running) {
                    break;
                }

                System.out.println("Error accepting TCP connection.");
                System.out.println(ex);
            }
        }
    }

    /**
     * Inicia a recepção de mensagens de uma conexão TCP.
     *
     * <p>A recepção é executada de forma independente para que uma conexão
     * não impeça a aceitação ou o processamento de outras conexões.</p>
     *
     * <p>Cada mensagem recebida é encaminhada aos plugins da aplicação.</p>
     *
     * @param address endereço do peer associado à conexão.
     * @param connection conexão TCP utilizada para a comunicação.
     */
    private void startReceiver(InetAddress address, TcpConnection connection) {
        connectionExecutor.submit(() -> {
            try {
                while (running) {
                    byte[] message = connection.receive();

                    for (Plugin plugin : main.getPlugins()) {
                        plugin.receiveMessage(message);
                    }
                }

            } catch (IOException ex) {
                System.out.println(
                        "Connection with " + address.getHostAddress() + " closed."
                );

            } finally {
                connections.remove(address, connection);

                try {
                    connection.close();
                } catch (IOException ignored) {
                }
            }
        });
    }

    /**
     * Inicia o processo de recepção de conexões TCP.
     */
    @Override
    public void run() {
        receive();
    }

    /**
     * Encerra o servidor TCP e as conexões associadas.
     *
     * <p>As conexões existentes são encerradas e o servidor deixa
     * de aceitar novas conexões.</p>
     */
    public void close() {
        running = false;

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException _) {
            }
        }

        for (TcpConnection connection : connections.values()) {
            try {
                connection.close();
            } catch (IOException _) {
            }
        }

        connections.clear();

        connectionExecutor.shutdownNow();
    }

    /**
     * Atualiza as conexões de acordo com os peers atualmente ativos.
     *
     * <p>Conexões associadas a peers que não estão mais ativos são encerradas
     * e removidas.</p>
     *
     * @param peers lista atualizada de peers ativos.
     */
    @Override
    public void onPeersChanged(List<PeerInfo> peers) {
        Set<InetAddress> activePeers = peers.stream()
                .map(PeerInfo::getAddress)
                .collect(Collectors.toSet());

        for (InetAddress address : connections.keySet()) {
            if (!activePeers.contains(address)) {

                TcpConnection connection = connections.remove(address);
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }
}

