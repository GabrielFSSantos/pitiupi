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
     * <p>O servidor é criado na porta definida pela aplicação. As conexões
     * com os peers são estabelecidas sob demanda durante o envio ou aceitas
     * quando iniciadas por outros peers.</p>
     *
     * @param main janela principal da aplicação, usada para obter a porta,
     *             o identificador local e os plugins disponíveis.
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

    /**
     * Envia uma mensagem por meio de uma conexão TCP pública com o peer de destino.
     *
     * <p>A conexão pública existente para o endereço informado é reutilizada.
     * Caso não exista uma conexão válida, uma nova conexão é estabelecida antes
     * do envio. Falhas durante o estabelecimento ou envio são registradas na
     * saída padrão e não são propagadas ao chamador.</p>
     *
     * @param msg mensagem serializada em bytes que será enviada.
     * @param destinationAddress endereço IP do peer que receberá a mensagem.
     */
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

    /**
     * Estabelece uma conexão TCP privada com um plugin específico do peer de destino.
     *
     * <p>A conexão é criada como privada e passa a ser gerenciada pelo
     * {@link ConnectionManager}. O método também inicia a tarefa responsável
     * por receber os dados dessa conexão.</p>
     *
     * @param destinationAddress endereço IP do peer de destino.
     * @param senderId identificador do remetente da conexão.
     * @param receiverId identificador do plugin que receberá os dados.
     * @return a conexão privada estabelecida ou {@code null} quando a criação
     *         da conexão falhar.
     * @throws IllegalArgumentException se o endereço de destino for o endereço
     *         local desta instância.
     */
    public TcpConnection createPrivateConnection(InetAddress destinationAddress, String senderId, String receiverId) {
        return establishOutgoingConnection(destinationAddress, senderId, receiverId, true);
    }

    /**
     * Obtém uma conexão TCP pública existente com o peer de destino ou estabelece
     * uma nova caso não exista uma conexão válida.
     *
     * <p>As conexões públicas são gerenciadas pelo {@link ConnectionManager}.
     * O método primeiro verifica se já existe uma conexão pública registrada
     * para o endereço de destino e, caso ela exista e não esteja fechada,
     * reutiliza-a. Caso contrário, uma nova conexão pública é estabelecida,
     * registrada no {@link ConnectionManager} e disponibilizada para uso.</p>
     *
     * <p>O acesso ao método é sincronizado para evitar que múltiplas threads
     * estabeleçam simultaneamente conexões públicas para o mesmo peer.</p>
     *
     * @param destinationAddress endereço IP do peer de destino.
     * @param senderId identificador usado como remetente no estabelecimento da conexão.
     * @param receiverId identificador usado como destinatário no estabelecimento da conexão.
     * @return uma conexão pública válida ou {@code null} caso não seja possível
     * estabelecê-la.
     */
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
     * <p>Cada socket aceito é encaminhado para processamento assíncrono,
     * interpretado como uma {@link TcpConnection}, registrado pelo
     * {@link ConnectionManager} e associado ao receptor adequado.</p>
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

    /**
     * Estabelece uma conexão TCP de saída com outro peer.
     *
     * <p>O método impede conexões com o próprio endereço local, cria a conexão
     * por meio da {@link ConnectionFactory}, registra-a no gerenciador de
     * conexões e inicia uma tarefa para receber dados continuamente.</p>
     *
     * @param destinationAddress endereço IP do peer de destino.
     * @param sender identificador do remetente.
     * @param receiver identificador do destinatário.
     * @param isPrivate {@code true} para uma conexão destinada a um plugin
     *                  específico; {@code false} para uma conexão pública.
     * @return a conexão estabelecida ou {@code null} se a
     *         {@link ConnectionFactory} não conseguir criá-la.
     * @throws IllegalArgumentException se o destino for o endereço desta máquina.
     */
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

    /**
     * Agenda o processamento de uma conexão TCP recebida.
     *
     * <p>A conexão é interpretada pela {@link ConnectionFactory}, registrada
     * no gerenciador e associada a uma tarefa de recepção. O processamento é
     * executado de forma assíncrona pelo executor compartilhado.</p>
     *
     * @param socket socket aceito pelo {@link ServerSocket}.
     */
    private void acceptIncomingConnection(Socket socket) {
        connectionExecutor.submit(() -> {
            TcpConnection connection = ConnectionFactory.receiveConnection(socket);
            if (connection == null) return;

            saveConnection(connection);
            startReceiver(connection, true);
        });
    }

    /**
     * Registra uma conexão no gerenciador de acordo com seu tipo.
     *
     * <p>Conexões privadas são armazenadas na coleção de conexões privadas;
     * as demais são armazenadas na coleção de conexões públicas.</p>
     *
     * @param connection conexão que será registrada.
     */
    private void saveConnection(TcpConnection connection) {
        if (connection.isPrivate()) {
            connectionManager.addPrivateConnection(connection);
        }
        else  {
            connectionManager.addPublicConnection(connection);
        }
    }

    /**
     * Seleciona e inicia o receptor adequado para uma conexão.
     *
     * <p>Conexões públicas são tratadas pelo receptor de mensagens públicas.
     * Conexões privadas destinadas ao próprio sistema são tratadas
     * separadamente; as demais são encaminhadas ao plugin identificado como
     * destinatário. Quando solicitado, o plugin também é notificado sobre o
     * estabelecimento da conexão.</p>
     *
     * @param connection conexão cujo fluxo de entrada será processado.
     * @param notify indica se o plugin destinatário deve ser notificado sobre
     *               o recebimento de uma nova conexão privada.
     */
    private void startReceiver(TcpConnection connection, boolean notify) {
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

    /**
     * Localiza o plugin responsável por receber uma conexão privada.
     *
     * <p>A busca é feita pelo identificador do destinatário informado na
     * própria conexão. Caso nenhum plugin correspondente seja encontrado,
     * uma mensagem de erro é enviada ao peer remoto.</p>
     *
     * @param connection conexão privada cujo destinatário será localizado.
     * @return o plugin com o identificador correspondente ao destinatário,
     *         ou {@code null} se nenhum plugin for encontrado.
     */
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

    /**
     * Processa continuamente as mensagens recebidas em uma conexão pública.
     *
     * <p>As mensagens são desserializadas e, quando são instâncias de
     * {@link Message}, encaminhadas a todos os plugins da aplicação. Mensagens
     * de controle do sistema podem encerrar a conexão ou registrar uma
     * condição de erro.</p>
     *
     * <p>O método também trata timeouts e erros de I/O, removendo a conexão
     * do {@link ConnectionManager} quando ela deixa de ser utilizável.</p>
     *
     * @param connection conexão pública da qual as mensagens serão recebidas.
     */
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

    /**
     * Processa os dados recebidos em uma conexão privada e os encaminha
     * ao plugin destinatário.
     *
     * <p>Dados que não representam mensagens de controle são entregues
     * diretamente ao {@link PrivateConnectionPlugin}. Mensagens de controle
     * relacionadas ao encerramento ou a erros internos provocam a notificação
     * do plugin e o fechamento da conexão.</p>
     *
     * @param connection conexão privada da qual os dados serão recebidos.
     * @param plugin plugin responsável pelo processamento dos dados recebidos.
     */
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

    /**
     * Processa mensagens recebidas em uma conexão destinada ao próprio sistema.
     *
     * <p>Atualmente, mensagens destinadas ao sistema são apenas verificadas
     * quanto ao seu formato de controle. Não há processamento adicional de
     * mensagens de sistema neste método.</p>
     *
     * @param connection conexão destinada ao identificador da própria aplicação.
     */
    private void systemConnectionReceiver(TcpConnection connection) {
        while (running) {
            try {
                byte[] message = connection.receive();
                try (ByteArrayInputStream bis = new ByteArrayInputStream(message);
                     ObjectInputStream ois = new ObjectInputStream(bis)) {

                    SysInfoMessage sysMessage = SysInfoMessage.parseSystemInfo(message);
                    if (sysMessage != null) continue;

                    //TODO: Ainda não existem mensagens trocadas entre o sistema.
                }

            }
            catch (SocketTimeoutException ex) {
                if (connection.hasTimedOut()) return;
            }
            catch (IOException ex) {
                connectionManager.closeConnection(connection);
            }
        }
    }

    /**
     * Encerra uma conexão TCP e registra o motivo informado.
     *
     * <p>Quando nenhuma descrição é fornecida, é utilizado um motivo padrão.
     * O fechamento efetivo da conexão é delegado ao {@link ConnectionManager}.</p>
     *
     * @param connection conexão que será encerrada.
     * @param description motivo do encerramento; pode ser {@code null}.
     */
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

    /**
     * Encerra o servidor TCP e libera os recursos associados.
     *
     * <p>Interrompe o laço de aceitação de conexões, fecha o
     * {@link ServerSocket}, encerra todas as conexões gerenciadas e interrompe
     * as tarefas do executor.</p>
     *
     * <p>Após a chamada, esta instância não deve ser reutilizada para aceitar
     * novas conexões.</p>
     */
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