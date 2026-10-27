package pitiupi.application.messaging;

import java.net.InetAddress;

/**
 * Destino de envio: sala geral ou um endereço IP.
 */
public sealed interface SendTarget permits SendTarget.BroadcastAll, SendTarget.SinglePeer {

    record BroadcastAll() implements SendTarget {
    }

    record SinglePeer(InetAddress address) implements SendTarget {
    }

    static SendTarget broadcastAll() {
        return new BroadcastAll();
    }

    static SendTarget singlePeer(InetAddress address) {
        return new SinglePeer(address);
    }
}
