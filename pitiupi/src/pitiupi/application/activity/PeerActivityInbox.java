package pitiupi.application.activity;

import pitiupi.api.plugin.PeerActivityFocusTokens;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pendências de atividade por peer: contagem (chat) ou indicador (screencast).
 */
public final class PeerActivityInbox {

    public static final String BROADCAST_PEER_KEY = "__broadcast__";

    private final Map<String, Map<String, Integer>> countedByPeerAndAction = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> indicatorByPeerAndAction = new ConcurrentHashMap<>();
    private PeerActivityFocus focus;
    private Runnable onChanged = () -> {
    };

    public void setFocus(PeerActivityFocus focus) {
        this.focus = focus;
        if (focus != null) {
            focus.bindInbox(this);
        }
    }

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged != null ? onChanged : () -> {
        };
    }

    /** @deprecated use {@link #recordCounted} */
    @Deprecated
    public void record(String peerInstanceId, String pluginId, String peerActionId) {
        recordCounted(peerInstanceId, pluginId, peerActionId, null);
    }

    public void recordBroadcast(String pluginId, String peerActionId) {
        recordCounted(BROADCAST_PEER_KEY, pluginId, peerActionId, PeerActivityFocusTokens.broadcast());
    }

    public void recordCounted(String peerInstanceId, String pluginId, String peerActionId, String focusToken) {
        if (shouldSkip(pluginId, focusToken)) {
            return;
        }
        String peerKey = peerKey(peerInstanceId);
        String actionKey = actionKey(pluginId, peerActionId);
        countedByPeerAndAction.computeIfAbsent(peerKey, k -> new ConcurrentHashMap<>())
                .merge(actionKey, 1, Integer::sum);
        notifyChanged();
    }

    public void recordBroadcastCounted(String pluginId, String peerActionId, String focusToken) {
        recordCounted(BROADCAST_PEER_KEY, pluginId, peerActionId, focusToken);
    }

    public void signal(String peerInstanceId, String pluginId, String peerActionId, String focusToken) {
        if (shouldSkip(pluginId, focusToken)) {
            return;
        }
        String peerKey = peerKey(peerInstanceId);
        String actionKey = actionKey(pluginId, peerActionId);
        indicatorByPeerAndAction.computeIfAbsent(peerKey, k -> new ConcurrentHashMap<>())
                .put(actionKey, 1);
        notifyChanged();
    }

    public void signalBroadcast(String pluginId, String peerActionId, String focusToken) {
        signal(BROADCAST_PEER_KEY, pluginId, peerActionId, focusToken);
    }

    public int totalCountedForPeer(String peerInstanceId) {
        return sum(countedByPeerAndAction.getOrDefault(peerKey(peerInstanceId), Map.of()));
    }

    /** @deprecated use {@link #totalCountedForPeer} */
    @Deprecated
    public int totalForPeer(String peerInstanceId) {
        return totalCountedForPeer(peerInstanceId);
    }

    public boolean hasIndicatorForPeer(String peerInstanceId) {
        Map<String, Integer> map = indicatorByPeerAndAction.get(peerKey(peerInstanceId));
        return map != null && !map.isEmpty();
    }

    public int countForAction(String peerInstanceId, String pluginId, String peerActionId) {
        return countedByPeerAndAction
                .getOrDefault(peerKey(peerInstanceId), Map.of())
                .getOrDefault(actionKey(pluginId, peerActionId), 0);
    }

    public boolean hasIndicatorForAction(String peerInstanceId, String pluginId, String peerActionId) {
        return indicatorByPeerAndAction
                .getOrDefault(peerKey(peerInstanceId), Map.of())
                .containsKey(actionKey(pluginId, peerActionId));
    }

    public void clearAction(String peerInstanceId, String pluginId, String peerActionId) {
        String peerKey = peerKey(peerInstanceId);
        String actionKey = actionKey(pluginId, peerActionId);
        removeAction(peerKey, actionKey);
        notifyChanged();
    }

    void clearForPluginFocus(String pluginId, String focusToken) {
        String peerKey = peerKeyFromFocusToken(focusToken);
        if (peerKey == null || pluginId == null) {
            return;
        }
        String prefix = pluginId + ":";
        clearPluginActionsOnPeer(peerKey, prefix);
        notifyChanged();
    }

    private void clearPluginActionsOnPeer(String peerKey, String pluginIdPrefix) {
        Map<String, Integer> counted = countedByPeerAndAction.get(peerKey);
        if (counted != null) {
            counted.keySet().removeIf(k -> k.startsWith(pluginIdPrefix));
            if (counted.isEmpty()) {
                countedByPeerAndAction.remove(peerKey);
            }
        }
        Map<String, Integer> indicators = indicatorByPeerAndAction.get(peerKey);
        if (indicators != null) {
            indicators.keySet().removeIf(k -> k.startsWith(pluginIdPrefix));
            if (indicators.isEmpty()) {
                indicatorByPeerAndAction.remove(peerKey);
            }
        }
    }

    private void removeAction(String peerKey, String actionKey) {
        Map<String, Integer> counted = countedByPeerAndAction.get(peerKey);
        if (counted != null) {
            counted.remove(actionKey);
            if (counted.isEmpty()) {
                countedByPeerAndAction.remove(peerKey);
            }
        }
        Map<String, Integer> indicators = indicatorByPeerAndAction.get(peerKey);
        if (indicators != null) {
            indicators.remove(actionKey);
            if (indicators.isEmpty()) {
                indicatorByPeerAndAction.remove(peerKey);
            }
        }
    }

    private boolean shouldSkip(String pluginId, String focusToken) {
        return focus != null
                && focusToken != null
                && !focusToken.isBlank()
                && focus.isFocused(pluginId, focusToken);
    }

    private void notifyChanged() {
        onChanged.run();
    }

    private static int sum(Map<String, Integer> map) {
        int total = 0;
        for (Integer v : map.values()) {
            total += v;
        }
        return total;
    }

    static String peerKeyFromFocusToken(String focusToken) {
        if (focusToken == null || focusToken.isBlank()) {
            return null;
        }
        if (PeerActivityFocusTokens.BROADCAST.equals(focusToken)) {
            return BROADCAST_PEER_KEY;
        }
        if (focusToken.startsWith("peer:")) {
            return focusToken.substring("peer:".length());
        }
        if (focusToken.startsWith("peer-ip:")) {
            return BROADCAST_PEER_KEY;
        }
        return null;
    }

    private static String peerKey(String peerInstanceId) {
        if (BROADCAST_PEER_KEY.equals(peerInstanceId)) {
            return BROADCAST_PEER_KEY;
        }
        return peerInstanceId != null ? peerInstanceId : BROADCAST_PEER_KEY;
    }

    private static String actionKey(String pluginId, String peerActionId) {
        return pluginId + ":" + peerActionId;
    }
}
