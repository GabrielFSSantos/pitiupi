package pitiupi.application.peeractions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro de peer actions por plugin; lista lateral pode exibir todos os plugins.
 */
public final class PeerActionBroker {

    private final Map<String, List<PeerAction>> actionsByPlugin = new ConcurrentHashMap<>();
    private final Map<String, String> displayNameByPlugin = new ConcurrentHashMap<>();
    private volatile String activePluginId;

    public void registerPlugin(String pluginId) {
        actionsByPlugin.putIfAbsent(pluginId, new ArrayList<>());
        if (activePluginId == null) {
            activePluginId = pluginId;
        }
    }

    public void registerPluginMetadata(String pluginId, String displayName) {
        displayNameByPlugin.put(pluginId, displayName);
    }

    public void setActivePlugin(String pluginId) {
        this.activePluginId = pluginId;
    }

    public String getActivePluginId() {
        return activePluginId;
    }

    public void registerAction(String pluginId, PeerAction action) {
        actionsByPlugin.computeIfAbsent(pluginId, k -> new ArrayList<>()).add(action);
    }

    public List<PeerAction> getActionsFor(PeerActionTarget target) {
        if (activePluginId == null) {
            return List.of();
        }
        return filterActions(actionsByPlugin.getOrDefault(activePluginId, List.of()), target);
    }

    public List<GroupedPeerActions> getAllActionsFor(PeerActionTarget target) {
        List<GroupedPeerActions> groups = new ArrayList<>();
        List<String> pluginIds = new ArrayList<>(actionsByPlugin.keySet());
        pluginIds.sort(Comparator.comparing(id -> displayNameByPlugin.getOrDefault(id, id)));

        for (String pluginId : pluginIds) {
            List<PeerAction> filtered = filterActions(actionsByPlugin.getOrDefault(pluginId, List.of()), target);
            if (!filtered.isEmpty()) {
                groups.add(new GroupedPeerActions(
                        pluginId,
                        displayNameByPlugin.getOrDefault(pluginId, pluginId),
                        filtered
                ));
            }
        }
        return Collections.unmodifiableList(groups);
    }

    private static List<PeerAction> filterActions(List<PeerAction> registered, PeerActionTarget target) {
        List<PeerAction> filtered = new ArrayList<>();
        for (PeerAction action : registered) {
            if (action.supports(target)) {
                filtered.add(action);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public void clearPlugin(String pluginId) {
        actionsByPlugin.remove(pluginId);
        displayNameByPlugin.remove(pluginId);
        if (pluginId.equals(activePluginId)) {
            activePluginId = actionsByPlugin.keySet().stream().findFirst().orElse(null);
        }
    }
}
