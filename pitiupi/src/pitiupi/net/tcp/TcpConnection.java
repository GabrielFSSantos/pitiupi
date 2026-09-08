package pitiupi.net.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Representa uma conexão TCP persistente com um peer.
 *
 * <p>Encapsula o socket e os streams utilizados para enviar e receber
 * mensagens. Também é responsável pelo framing das mensagens, permitindo
 * que várias mensagens sejam transmitidas pela mesma conexão.</p>
 *
 * @author Gustavo
 */
public class TcpConnection {
    private static final long TIMEOUT = 300_000; // 5 minutos

    private final Socket socket;
    private final DataInputStream input;
    private final DataOutputStream output;
    private final InetAddress address;
    private String sender;
    private String receiver;

    private boolean isPrivate;

    private volatile long lastActivity;

    TcpConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.input = new DataInputStream(socket.getInputStream());
        this.output = new DataOutputStream(socket.getOutputStream());
        this.address = socket.getInetAddress();
        this.lastActivity = System.currentTimeMillis();
    }

    public synchronized void send(byte[] message) throws IOException {
        output.writeInt(message.length);
        output.write(message);
        output.flush();
        updateLastActivity();
    }

    byte[] receive() throws IOException {
        int length = input.readInt();

        if (length < 0) throw new IOException("Invalid message length.");

        byte[] message = input.readNBytes(length);

        if (message.length != length) {
            throw new IOException("Connection closed before receiving the complete message.");
        }

        updateLastActivity();

        return message;
    }

    public boolean isClosed() {
        return socket.isClosed();
    }

    void close() throws IOException {
        socket.close();
    }

    public InetAddress getAddress() {
        return address;
    }

    private void updateLastActivity() {
        lastActivity = System.currentTimeMillis();
    }

    void setTimeout(int timeout) throws SocketException {
        socket.setSoTimeout(timeout);
    }

    boolean hasTimedOut() {
        return System.currentTimeMillis() - lastActivity >= TIMEOUT;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getSender() {
        return sender;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    void setSender(String sender) {
        this.sender = sender;
    }

    void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
