package pitiupi.presentation.shell;

import pitiupi.application.presence.UserPeerView;
import pitiupi.control.ConnectionManager;

import java.net.InetAddress;

/**
 * Nomes amigáveis para abas e banner de sessão com peers.
 */
public final class SessionPeerLabels {

    private SessionPeerLabels() {
    }

    public static String resolveDisplayName(ConnectionManager connectionManager, String ip, String hintName) {
        if (ip == null || ip.isBlank()) {
            return hintName != null && !hintName.isBlank() ? hintName : "";
        }
        String trimmedIp = ip.trim();
        if (hintName != null && !hintName.isBlank() && !hintName.trim().equals(trimmedIp)) {
            return hintName.trim();
        }
        for (UserPeerView view : connectionManager.getPresenceService().listPeerViews()) {
            if (view.isBroadcastTarget()) {
                continue;
            }
            if (view.isSelf() && isLocalIp(trimmedIp)) {
                return selfLabel(connectionManager);
            }
            if (view.getInetAddress().map(InetAddress::getHostAddress).orElse("").equals(trimmedIp)) {
                String name = view.getDisplayName();
                if (name != null && !name.isBlank() && !name.equals(trimmedIp)) {
                    return name;
                }
                if (view.isSelf()) {
                    return selfLabel(connectionManager);
                }
            }
        }
        if (isLocalIp(trimmedIp)) {
            return selfLabel(connectionManager);
        }
        return trimmedIp;
    }

    public static String tabTitle(ConnectionManager connectionManager, String ip, String hintName) {
        return resolveDisplayName(connectionManager, ip, hintName);
    }

    public static String sessionBannerHtml(String displayName, String ip) {
        String safeIp = ip != null ? ip.trim() : "";
        String who = displayName != null && !displayName.isBlank() ? displayName.trim() : safeIp;
        if (safeIp.isEmpty()) {
            return "<html><b>Sessão:</b> " + escape(who) + "</html>";
        }
        if (who.equals(safeIp)) {
            return "<html><b>Sessão:</b> IP " + escape(safeIp) + "</html>";
        }
        return "<html><b>Sessão:</b> " + escape(who) + " · IP " + escape(safeIp) + "</html>";
    }

    private static String selfLabel(ConnectionManager connectionManager) {
        String name = connectionManager.getUserName();
        if (name == null || name.isBlank()) {
            return "Você";
        }
        return name + " (você)";
    }

    private static boolean isLocalIp(String ip) {
        return LocalNetworkInfo.isLocalIpv4(ip);
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;");
    }
}
