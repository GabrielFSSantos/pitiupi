package pitiupi.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Representa uma conexão TCP persistente com um peer.
 *
 * <p>Encapsula o socket e os streams utilizados para enviar e receber
 * mensagens. Também é responsável pelo framing das mensagens, permitindo
 * que várias mensagens sejam transmitidas pela mesma conexão.</p>
 *
 * @author Gustavo
 */
class TcpConnection {

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;

    /**
     * Cria uma conexão a partir de um socket existente.
     *
     * @param socket socket TCP utilizado pela conexão.
     * @throws IOException caso não seja possível obter os streams do socket.
     */
    public TcpConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Envia uma mensagem pela conexão.
     *
     * <p>A mensagem é precedida por seu tamanho para permitir que o receptor
     * identifique o limite entre mensagens transmitidas pela mesma conexão.</p>
     *
     * @param message mensagem serializada a ser enviada.
     * @throws IOException caso ocorra um erro durante o envio.
     */
    public void send(byte[] message) throws IOException {
        synchronized (output) {
            output.writeInt(message.length);
            output.write(message);
            output.flush();
        }
    }

    /**
     * Recebe uma mensagem completa da conexão.
     *
     * <p>O tamanho da mensagem é lido primeiro e, em seguida, os dados
     * correspondentes são recebidos.</p>
     *
     * @return mensagem recebida.
     * @throws IOException caso ocorra um erro durante a recepção.
     */
    public byte[] receive() throws IOException {
        int length = input.readInt();

        if (length < 0) {
            throw new IOException("Invalid message length.");
        }

        return input.readNBytes(length);
    }

    /**
     * Verifica se o socket da conexão está fechado.
     *
     * @return {@code true} caso o socket esteja fechado.
     */
    public boolean isClosed() {
        return socket.isClosed();
    }

    /**
     * Encerra a conexão TCP.
     *
     * @throws IOException caso ocorra um erro ao fechar o socket.
     */
    public void close() throws IOException {
        socket.close();
    }
}
