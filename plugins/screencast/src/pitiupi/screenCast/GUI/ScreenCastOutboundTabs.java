package pitiupi.screenCast.GUI;

import pitiupi.presentation.shell.PluginSessionKeys;

/**
 * Abas em que o usuário pode ligar envio de tela (outbound).
 */
final class ScreenCastOutboundTabs {

    private ScreenCastOutboundTabs() {
    }

    static boolean isOutboundShareTabKey(String key) {
        if (key == null || key.isBlank()) {
            return false;
        }
        if (PluginSessionKeys.PEER_DRAFT.equals(key)) {
            return false;
        }
        if (key.startsWith("view:")) {
            return false;
        }
        return PluginSessionKeys.ROOM.equals(key) || key.startsWith("peer:");
    }
}
