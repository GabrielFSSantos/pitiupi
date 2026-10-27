package pitiupi.application.presence;

import java.net.InetAddress;
import java.util.Optional;

/**
 * DTO de peer para a UI (lista lateral e peer actions futuras).
 */
public final class UserPeerView {

    private final String displayName;
    private final String instanceId;
    private final InetAddress inetAddress;
    private final int notificationCount;
    private final boolean isSelf;
    private final boolean isBroadcastTarget;

    public UserPeerView(
            String displayName,
            String instanceId,
            InetAddress inetAddress,
            int notificationCount,
            boolean isSelf,
            boolean isBroadcastTarget
    ) {
        this.displayName = displayName;
        this.instanceId = instanceId;
        this.inetAddress = inetAddress;
        this.notificationCount = notificationCount;
        this.isSelf = isSelf;
        this.isBroadcastTarget = isBroadcastTarget;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Optional<InetAddress> getInetAddress() {
        return Optional.ofNullable(inetAddress);
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public boolean isBroadcastTarget() {
        return isBroadcastTarget;
    }

    public static UserPeerView broadcastTarget() {
        return new UserPeerView("Todos", null, null, 0, false, true);
    }
}
