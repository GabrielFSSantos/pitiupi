package pitiupi.application.activity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pendências de atividade por peer (mensagem, tela, etc.) para badges na lista lateral.
 */
public final class PeerActivityInbox {

    public static final String BROADCAST_PEER_KEY = "__broadcast__";

    private final Map<String, Map<String, Integer>> countByPeerAndAction = new ConcurrentHashMap<>();
    private Runnable onChanged = () -> {
    };

    public void setOnChanged(Runnable onChanged) {
        this.onChanged = onChanged != null ? onChanged : () -> {
        };
    }

    public void record(String peerInstanceId, String pluginId, String peerActionId) {
        String peerKey = peerKey(peerInstanceId);
        String actionKey = actionKey(pluginId, peerActionId);
        countByPeerAndAction.computeIfAbsent(peerKey, k -> new ConcurrentHashMap<>())
                .merge(actionKey, 1, Integer::sum);
        onChanged.run();
    }

    public void recordBroadcast(String pluginId, String peerActionId) {
        record(BROADCAST_PEER_KEY, pluginId, peerActionId);
    }

    public int totalForPeer(String peerInstanceId) {
        return sum(countByPeerAndAction.getOrDefault(peerKey(peerInstanceId), Map.of()));
    }

    public int countForAction(String peerInstanceId, String pluginId, String peerActionId) {
        return countByPeerAndAction
                .getOrDefault(peerKey(peerInstanceId), Map.of())
                .getOrDefault(actionKey(pluginId, peerActionId), 0);
    }

    public void clearAction(String peerInstanceId, String pluginId, String peerActionId) {
        Map<String, Integer> actions = countByPeerAndAction.get(peerKey(peerInstanceId));
        if (actions != null) {
            actions.remove(actionKey(pluginId, peerActionId));
            if (actions.isEmpty()) {
                countByPeerAndAction.remove(peerKey(peerInstanceId));
            }
        }
        onChanged.run();
    }

    private static int sum(Map<String, Integer> map) {
        int total = 0;
        for (Integer v : map.values()) {
            total += v;
        }
        return total;
    }

    private static String peerKey(String peerInstanceId) {
        return peerInstanceId != null ? peerInstanceId : BROADCAST_PEER_KEY;
    }

    private static String actionKey(String pluginId, String peerActionId) {
        return pluginId + ":" + peerActionId;
    }
}
