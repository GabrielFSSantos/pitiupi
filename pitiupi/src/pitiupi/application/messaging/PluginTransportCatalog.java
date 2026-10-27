package pitiupi.application.messaging;

import java.util.List;

/**
 * Transportes expostos na UI por tipo de plugin e destino (didático).
 */
public final class PluginTransportCatalog {

    public enum PluginKind {
        CHAT,
        SCREEN_CAST,
        PING
    }

    private PluginTransportCatalog() {
    }

    public static List<TransportMode> forBroadcast(PluginKind kind) {
        return switch (kind) {
            case CHAT -> List.of(
                    TransportMode.MULTICAST_UDP,
                    TransportMode.UNICAST_UDP,
                    TransportMode.TCP
            );
            case SCREEN_CAST, PING -> List.of(
                    TransportMode.MULTICAST_UDP,
                    TransportMode.UNICAST_UDP
            );
        };
    }

    public static List<TransportMode> forSinglePeer(PluginKind kind) {
        return switch (kind) {
            case CHAT, SCREEN_CAST -> List.of(
                    TransportMode.UNICAST_UDP,
                    TransportMode.TCP
            );
            case PING -> List.of(TransportMode.UNICAST_UDP);
        };
    }
}
