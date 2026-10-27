package pitiupi.application.messaging;

/**
 * Rótulos de destino na UI (chat, screencast, etc.).
 */
public final class MessagingDestinationUi {

    public static final String EVERYONE_IN_ROOM = "Todos na sala";
    public static final String SINGLE_PERSON = "Para uma pessoa (IP)";

    private static final String PRIVATE_TCP_SUMMARY =
            "Conversa só entre vocês (TCP privado)";

    private MessagingDestinationUi() {
    }

    public static String summarizeSendMode(boolean everyoneInRoom, TransportMode mode, boolean privateTcp) {
        if (!everyoneInRoom && privateTcp) {
            return PRIVATE_TCP_SUMMARY;
        }
        if (mode == null) {
            return "";
        }
        return mode.describeForUi(everyoneInRoom);
    }
}
