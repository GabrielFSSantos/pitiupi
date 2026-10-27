package pitiupi.application.activity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Canal de atividade em foco por plugin (sub-aba / sessão visível).
 */
public final class PeerActivityFocus {

    private final Map<String, String> focusedChannelByPlugin = new ConcurrentHashMap<>();
    private PeerActivityInbox inbox;

    public void bindInbox(PeerActivityInbox inbox) {
        this.inbox = inbox;
    }

    public void setFocusedChannel(String pluginId, String focusToken) {
        if (pluginId == null || pluginId.isBlank()) {
            return;
        }
        if (focusToken == null || focusToken.isBlank()) {
            focusedChannelByPlugin.remove(pluginId);
            return;
        }
        focusedChannelByPlugin.put(pluginId, focusToken);
        if (inbox != null) {
            inbox.clearForPluginFocus(pluginId, focusToken);
        }
    }

    public void clear(String pluginId) {
        if (pluginId != null) {
            focusedChannelByPlugin.remove(pluginId);
        }
    }

    public boolean isFocused(String pluginId, String focusToken) {
        if (pluginId == null || focusToken == null || focusToken.isBlank()) {
            return false;
        }
        String current = focusedChannelByPlugin.get(pluginId);
        return focusToken.equals(current);
    }
}
