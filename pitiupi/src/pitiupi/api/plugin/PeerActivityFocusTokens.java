package pitiupi.api.plugin;

import pitiupi.presentation.shell.PeerIpv4;

/**
 * Tokens de foco para suprimir badges enquanto o usuário vê o canal correspondente.
 */
public final class PeerActivityFocusTokens {

    public static final String BROADCAST = "broadcast";

    private PeerActivityFocusTokens() {
    }

    public static String broadcast() {
        return BROADCAST;
    }

    public static String peerInstance(String instanceId) {
        if (instanceId == null || instanceId.isBlank()) {
            return "";
        }
        return "peer:" + instanceId.trim();
    }

    public static String peerIp(String ip) {
        String normalized = PeerIpv4.normalize(ip != null ? ip : "");
        if (normalized.isEmpty()) {
            return "";
        }
        return "peer-ip:" + normalized;
    }
}
