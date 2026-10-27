package pitiupi.application.messaging;

import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Messages.Message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Envio didático multicast / unicast / TCP para sala geral ou um peer.
 */
public final class MessageDispatcher {

    private MessageDispatcher() {
    }

    public static int send(
            ConnectionManager connectionManager,
            Message message,
            SendTarget target,
            TransportMode mode
    ) {
        if (target instanceof SendTarget.BroadcastAll) {
            return sendToAll(connectionManager, message, mode);
        }
        if (target instanceof SendTarget.SinglePeer single) {
            return sendToOne(connectionManager, message, mode, single.address()) ? 1 : 0;
        }
        return 0;
    }

    private static int sendToAll(ConnectionManager connectionManager, Message message, TransportMode mode) {
        if (mode == TransportMode.MULTICAST_UDP) {
            return connectionManager.sendUdpMulticast(message) ? 1 : 0;
        }
        List<InetAddress> peers = onlinePeerAddresses(connectionManager);
        int sent = 0;
        for (InetAddress address : peers) {
            if (sendToOne(connectionManager, message, mode, address)) {
                sent++;
            }
        }
        return sent;
    }

    private static boolean sendToOne(
            ConnectionManager connectionManager,
            Message message,
            TransportMode mode,
            InetAddress address
    ) {
        if (address == null) {
            return false;
        }
        return switch (mode) {
            case MULTICAST_UDP -> connectionManager.sendUdpMulticast(message);
            case UNICAST_UDP -> connectionManager.sendUdpUnicast(message, address);
            case TCP -> connectionManager.sendTcp(message, address);
        };
    }

    static List<InetAddress> onlinePeerAddresses(ConnectionManager connectionManager) {
        List<InetAddress> addresses = new ArrayList<>();
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (view.isBroadcastTarget() || view.isSelf()) {
                continue;
            }
            view.getInetAddress().ifPresent(addresses::add);
        }
        return addresses;
    }
}
