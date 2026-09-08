package pitiupi.net.tcp;

import pitiupi.net.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionFactory {
    private static final int HANDSHAKE_TIMEOUT = 5000;
    private static final int STANDARD_TIMEOUT = 300_000;

    public static TcpConnection createConnection(InetAddress address, int port, String sender, String receiver, boolean isPrivate) {
        try {
            TcpConnection connection;

            Socket socket = new Socket(address, port);
            connection = new TcpConnection(socket);
            connection.setSender(sender);
            connection.setReceiver(receiver);
            connection.setPrivate(isPrivate);

            HandshakeMessage handshake = new HandshakeMessage(isPrivate, sender, receiver);

            connection.send(handshake.toByteArray());
            HandshakeMessage handshakeResponse = validateConnection(connection);

            if (handshakeResponse == null) return null;

            connection.setTimeout(STANDARD_TIMEOUT);

            return connection;
        }
        catch (IOException ex) {
            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static TcpConnection receiveConnection(Socket socket) {
        try {
            TcpConnection connection;

            connection = new TcpConnection(socket);

            HandshakeMessage handshake = validateConnection(connection);

            if (handshake == null) return null;

            connection.setSender(handshake.getSender());
            connection.setReceiver(handshake.getReceiver());
            connection.setPrivate(handshake.isPrivate());

            connection.setTimeout(STANDARD_TIMEOUT);

            return connection;
        }
        catch (IOException ex) {
            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static HandshakeMessage validateConnection(TcpConnection connection) {
        try {
            connection.setTimeout(HANDSHAKE_TIMEOUT);
            byte[] bytes = connection.receive();

            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);

            Object object = ois.readObject();

            if (object instanceof Message message) {
                if (SysInfoMessage.parseSystemInfo(message) != null) {
                    //TODO: Tratar erros
                    return null;
                }

                if (message instanceof HandshakeMessage handshake) {
                    return (validateHandshake(handshake, connection)) ? handshake : null;
                }
            }

            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.CONNECTION_REJECTED,
                    "The message was not from an allowed type."
            );
        }
        catch (SocketTimeoutException ex) {
            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.CONNECTION_REJECTED,
                    "No handshake was received within the allowed time."
            );
        }
        catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(ConnectionFactory.class.getName()).log(Level.SEVERE, null, ex);

            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.INTERNAL_ERROR,
                    "The server could not process the handshake."
            );
        }

        return null;
    }

    private static boolean validateHandshake(HandshakeMessage handshake, TcpConnection connection) {
        String sender = handshake.getSender();
        String receiver = handshake.getReceiver();

        if (sender == null || sender.isBlank()) {
            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.CONNECTION_REJECTED,
                    "Invalid Sender: " + sender + "."
            );
            return false;
        }
        if (receiver == null || receiver.isBlank()) {
            SysInfoMessage.sendSystemInfo(
                    connection,
                    SysInfoType.CONNECTION_REJECTED,
                    "Invalid Receiver: " + receiver + "."
            );
            return false;
        }
        return true;
    }
}
