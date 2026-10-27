package pitiupi.application.invites;

import pitiupi.api.plugin.InviteDetails;
import pitiupi.control.HeartbeatManager;
import pitiupi.models.Messages.SessionInviteMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Caixa de convites recebidos; incrementa contador por remetente na lista.
 */
public final class InviteInboxService {

    private final HeartbeatManager heartbeatManager;
    private final Map<String, Integer> notificationCountByFromInstance = new ConcurrentHashMap<>();
    private final Map<String, SessionInviteMessage> pendingByInviteId = new ConcurrentHashMap<>();
    private final Map<String, Consumer<InviteDetails>> handlersByPluginId = new ConcurrentHashMap<>();
    private Runnable onNotificationsChanged = () -> {
    };
    private volatile String lastAcceptedPluginId;

    public InviteInboxService(HeartbeatManager heartbeatManager) {
        this.heartbeatManager = heartbeatManager;
    }

    public void setOnNotificationsChanged(Runnable onNotificationsChanged) {
        this.onNotificationsChanged = onNotificationsChanged != null ? onNotificationsChanged : () -> {
        };
    }

    public void setInviteHandler(String pluginId, Consumer<InviteDetails> handler) {
        handlersByPluginId.put(pluginId, handler);
    }

    public void receive(SessionInviteMessage message) {
        if (message.getFromInstanceId().equals(heartbeatManager.getInstanceId())) {
            return;
        }
        String to = message.getToInstanceId();
        if (to != null && !to.isBlank() && !to.equals(heartbeatManager.getInstanceId())) {
            return;
        }

        pendingByInviteId.put(message.getInviteId(), message);
        notificationCountByFromInstance.merge(message.getFromInstanceId(), 1, Integer::sum);
        onNotificationsChanged.run();
    }

    public int getNotificationCount(String fromInstanceId) {
        return notificationCountByFromInstance.getOrDefault(fromInstanceId, 0);
    }

    public boolean acceptInvite(String inviteId) {
        SessionInviteMessage message = pendingByInviteId.remove(inviteId);
        if (message == null) {
            return false;
        }
        decrementNotification(message.getFromInstanceId());
        InviteDetails details = new InviteDetails(
                message.getInviteId(),
                message.getFromInstanceId(),
                message.getFromDisplayName(),
                message.getToInstanceId(),
                message.getPluginId(),
                message.getKind(),
                message.getPayload()
        );
        lastAcceptedPluginId = message.getPluginId();
        Consumer<InviteDetails> handler = handlersByPluginId.get(message.getPluginId());
        if (handler != null) {
            handler.accept(details);
        }
        onNotificationsChanged.run();
        return true;
    }

    public String acceptFirstInviteFrom(String fromInstanceId) {
        for (Map.Entry<String, SessionInviteMessage> e : pendingByInviteId.entrySet()) {
            if (e.getValue().getFromInstanceId().equals(fromInstanceId)) {
                if (acceptInvite(e.getKey())) {
                    return lastAcceptedPluginId;
                }
                return null;
            }
        }
        return null;
    }

    public String getLastAcceptedPluginId() {
        return lastAcceptedPluginId;
    }

    private void decrementNotification(String fromInstanceId) {
        notificationCountByFromInstance.computeIfPresent(fromInstanceId, (k, v) -> v <= 1 ? null : v - 1);
    }
}
