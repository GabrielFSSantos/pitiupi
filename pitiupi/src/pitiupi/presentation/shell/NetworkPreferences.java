package pitiupi.presentation.shell;

import java.util.prefs.Preferences;

/**
 * Nome, porta, TTL e saída de rede no mesmo nó de preferências do login.
 */
public final class NetworkPreferences {

    public static final String PREF_NOME = "lastUserName";
    public static final String PREF_PORTA = "lastPort";
    public static final String PREF_TTL = "lastTtl";
    public static final String PREF_EGRESS = "lastEgress";

    public static final int DEFAULT_PORT = 5000;
    public static final int DEFAULT_TTL = 1;
    public static final int MIN_TTL = 1;
    public static final int MAX_TTL = 32;

    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginDialog.class);

    private NetworkPreferences() {
    }

    public static String loadName() {
        String fallback = System.getProperty("user.name");
        return PREFS.get(PREF_NOME, fallback == null ? "" : fallback);
    }

    public static int loadPort() {
        return PREFS.getInt(PREF_PORTA, DEFAULT_PORT);
    }

    public static int loadTtl() {
        return clampTtl(PREFS.getInt(PREF_TTL, DEFAULT_TTL));
    }

    public static int clampTtl(int ttl) {
        if (ttl < MIN_TTL) {
            return MIN_TTL;
        }
        if (ttl > MAX_TTL) {
            return MAX_TTL;
        }
        return ttl;
    }

    public static LocalNetworkInfo.EgressMode loadEgress() {
        return fromCode(PREFS.get(PREF_EGRESS, "auto"));
    }

    /**
     * Aplica a saída gravada antes de os sockets abrirem.
     */
    public static void applyEgressBeforeSockets() {
        LocalNetworkInfo.setEgressMode(loadEgress());
    }

    public static void saveName(String name) {
        PREFS.put(PREF_NOME, name);
    }

    public static void savePort(int port) {
        PREFS.putInt(PREF_PORTA, port);
    }

    public static void saveTtl(int ttl) {
        PREFS.putInt(PREF_TTL, clampTtl(ttl));
    }

    public static void saveEgress(LocalNetworkInfo.EgressMode mode) {
        PREFS.put(PREF_EGRESS, toCode(mode));
    }

    public static String toCode(LocalNetworkInfo.EgressMode mode) {
        if (mode == LocalNetworkInfo.EgressMode.WIRED) {
            return "wired";
        }
        if (mode == LocalNetworkInfo.EgressMode.WIRELESS) {
            return "wireless";
        }
        return "auto";
    }

    public static LocalNetworkInfo.EgressMode fromCode(String code) {
        if ("wired".equals(code)) {
            return LocalNetworkInfo.EgressMode.WIRED;
        }
        if ("wireless".equals(code)) {
            return LocalNetworkInfo.EgressMode.WIRELESS;
        }
        return LocalNetworkInfo.EgressMode.AUTO;
    }
}
