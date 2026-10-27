package pitiupi.presentation.shell;

/**
 * Sufixos visuais para distinguir abas de envio e recebimento no mesmo plugin.
 */
public final class SessionTabDirectionLabels {

    private SessionTabDirectionLabels() {
    }

    public static String outboundTitle(String displayName) {
        String base = displayName != null && !displayName.isBlank() ? displayName.trim() : "?";
        return base + " ↑";
    }

    public static String inboundTitle(String displayName) {
        String base = displayName != null && !displayName.isBlank() ? displayName.trim() : "?";
        return base + " ↓";
    }
}
