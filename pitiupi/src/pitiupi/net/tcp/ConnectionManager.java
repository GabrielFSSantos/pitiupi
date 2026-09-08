package pitiupi.net.tcp;

import pitiupi.control.PeerInfo;
import pitiupi.control.PeerListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ConnectionManager implements PeerListener {
    private final Logger logger;
    private final Map<InetAddress, TcpConnection> publicConnections;
    private final Map<InetAddress, List<TcpConnection>> privateConnections;

    public ConnectionManager() {
        publicConnections = new ConcurrentHashMap<>();
        privateConnections = new ConcurrentHashMap<>();

        logger = Logger.getLogger(getClass().getName());
    }

    public TcpConnection getPublicConnection(InetAddress address) {
        return publicConnections.get(address);
    }

    public void addPrivateConnection(InetAddress address, TcpConnection connection) {
        List<TcpConnection> connections =
                privateConnections.computeIfAbsent(connection.getAddress(), _ -> new ArrayList<>());

        synchronized (connections) {
            connections.add(connection);
        }
    }
    public void addPublicConnection(InetAddress address, TcpConnection connection) {
        TcpConnection oldConnection = publicConnections.put(connection.getAddress(), connection);
        if (oldConnection != null && oldConnection != connection) {
            closeConnection(oldConnection);
        }
    }

    public void closeConnection(TcpConnection connection, String reason) {
        SysInfoMessage.sendSystemInfo(connection, SysInfoType.CONNECTION_CLOSED, reason);
        closeConnection(connection);
    }
    public void closeConnection(TcpConnection connection) {
        if (connection == null) return;
        if (connection.isPrivate()) {
            List<TcpConnection> connections = privateConnections.get(connection.getAddress());

            if (connections != null) {
                synchronized (connections) {
                    connections.remove(connection);

                    if (connections.isEmpty()) privateConnections.remove(connection.getAddress(), connections);
                }
            }
        }
        else publicConnections.remove(connection.getAddress(), connection);

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error closing TCP connection", ex);
        }
    }

    public void closeByAdress(InetAddress address, String reason) {
        List<TcpConnection> connections = privateConnections.get(address);
        for (TcpConnection connection : connections) {
            SysInfoMessage.sendSystemInfo(connection, SysInfoType.CONNECTION_CLOSED, reason);
        }
        closeByAdress(address);
    }

    public void closeByAdress(InetAddress address) {
        TcpConnection publicConnection = publicConnections.remove(address);

        if (publicConnection != null) {
            try {
                publicConnection.close();
            }
            catch (IOException ex) {
                logger.log(Level.SEVERE, "Error closing TCP connection", ex);
            }

        }

        List<TcpConnection> connections = privateConnections.remove(address);

        if (connections != null) {
            synchronized (connections) {
                for (TcpConnection connection : connections) {
                    try {
                        connection.close();
                    }
                    catch (IOException ex) {
                        logger.log(Level.SEVERE, "Error closing TCP connection", ex);
                    }
                }
                connections.clear();
            }
        }
    }

    public void closeAllConnections(String reason) {
        for (List<TcpConnection> connections : privateConnections.values()) {
            for (TcpConnection connection : connections) {
                SysInfoMessage.sendSystemInfo(connection, SysInfoType.CONNECTION_CLOSED, reason);
            }
        }
        closeAllConnections();
    }
    public void closeAllConnections() {
        for (TcpConnection connection : publicConnections.values()) {
            closeConnection(connection);
        }

        synchronized (privateConnections) {
            for (List<TcpConnection> connectionsList : privateConnections.values()) {
                for (TcpConnection connection : connectionsList) {
                    closeConnection(connection);
                }
                privateConnections.clear();
            }
        }
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
        Set<InetAddress> activePeers = peers.stream().map(PeerInfo::getAddress).collect(Collectors.toSet());

        for (InetAddress address : publicConnections.keySet()) {
            if (!activePeers.contains(address)) {
                closeByAdress(address);
            }
        }
        for (InetAddress address : privateConnections.keySet()) {
            if (!activePeers.contains(address)) {
                closeByAdress(address);
            }
        }
    }
}
