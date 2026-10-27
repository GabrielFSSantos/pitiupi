package pitiupi.presentation.shell;

import pitiupi.control.ConnectionManager;

/**
 * Identifica mensagens e atividade originadas neste host (eco de rede).
 */
public final class LocalUserIdentity {

    private LocalUserIdentity() {
    }

    public static boolean isLocalSender(ConnectionManager connectionManager, String senderName, String senderIp) {
        if (connectionManager == null || senderName == null) {
            return false;
        }
        String localName = connectionManager.getUserName();
        if (localName == null || !localName.equals(senderName.trim())) {
            return false;
        }
        if (senderIp == null || senderIp.isBlank()) {
            return false;
        }
        return LocalNetworkInfo.isLocalIpv4(canonicalIpv4(senderIp));
    }

    private static String canonicalIpv4(String ip) {
        try {
            return PeerIpv4.normalize(ip);
        } catch (Exception ex) {
            return ip.trim();
        }
    }
}
