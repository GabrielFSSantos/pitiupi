package pitiupi.application.session;

/**
 * Canal lógico de um aviso de sessão (extensível por plugins).
 */
public enum SessionNoticeChannel {
    SCREEN_CAST("screen_cast");

    private final String wireId;

    SessionNoticeChannel(String wireId) {
        this.wireId = wireId;
    }

    public String wireId() {
        return wireId;
    }

    public static SessionNoticeChannel fromWireId(String id) {
        if (id == null) {
            return null;
        }
        for (SessionNoticeChannel channel : values()) {
            if (channel.wireId.equals(id)) {
                return channel;
            }
        }
        return null;
    }
}
