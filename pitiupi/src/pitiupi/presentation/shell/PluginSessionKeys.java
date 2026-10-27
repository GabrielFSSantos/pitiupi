package pitiupi.presentation.shell;

/**
 * Chaves de abas de sessão (chat, screencast).
 */
public final class PluginSessionKeys {

    public static final String ROOM = "room";
    public static final String ADD_PEER = "__add_peer__";
    public static final String PEER_DRAFT = "peer:__draft__";

    private PluginSessionKeys() {
    }

    public static String peer(String ip) {
        return "peer:" + ip;
    }
}
