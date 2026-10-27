package pitiupi.chat.GUI;

import pitiupi.application.messaging.TransportMode;

/**
 * HTML do histórico com cores por transporte.
 */
public final class ChatTranscriptHtml {

    private ChatTranscriptHtml() {
    }

    public static String coloredTransportTag(String transportLabel) {
        if (transportLabel == null || transportLabel.isBlank()) {
            return "<span style='color:#616161'>[—]</span>";
        }
        String normalized = transportLabel.trim().toLowerCase();
        String color;
        if (normalized.contains("privado")) {
            color = "#6a1b9a";
        } else if (normalized.contains("multicast")) {
            color = "#2e7d32";
        } else if (normalized.contains("udp")) {
            color = "#1565c0";
        } else if (normalized.contains("tcp")) {
            color = "#e65100";
        } else {
            color = "#616161";
        }
        return "<span style='color:" + color + ";font-weight:bold'>[" + escape(transportLabel) + "]</span>";
    }

    public static String tagForMode(TransportMode mode) {
        return coloredTransportTag(mode != null ? mode.getLabel() : "—");
    }

    public static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String wrapDocument(String innerHtml) {
        if (innerHtml == null || innerHtml.isBlank()) {
            return "<html><body style='margin:0'></body></html>";
        }
        return "<html><body style='margin:0'>" + innerHtml + "</body></html>";
    }
}
