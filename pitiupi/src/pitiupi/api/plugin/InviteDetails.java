package pitiupi.api.plugin;

import pitiupi.application.invites.SessionInviteKind;

/**
 * Convite aceito pelo usuário na UI.
 */
public final class InviteDetails {

    private final String inviteId;
    private final String fromInstanceId;
    private final String fromDisplayName;
    private final String toInstanceId;
    private final String pluginId;
    private final SessionInviteKind kind;
    private final String payload;

    public InviteDetails(
            String inviteId,
            String fromInstanceId,
            String fromDisplayName,
            String toInstanceId,
            String pluginId,
            SessionInviteKind kind,
            String payload
    ) {
        this.inviteId = inviteId;
        this.fromInstanceId = fromInstanceId;
        this.fromDisplayName = fromDisplayName;
        this.toInstanceId = toInstanceId;
        this.pluginId = pluginId;
        this.kind = kind;
        this.payload = payload;
    }

    public String getInviteId() {
        return inviteId;
    }

    public String getFromInstanceId() {
        return fromInstanceId;
    }

    public String getFromDisplayName() {
        return fromDisplayName;
    }

    public String getToInstanceId() {
        return toInstanceId;
    }

    public String getPluginId() {
        return pluginId;
    }

    public SessionInviteKind getKind() {
        return kind;
    }

    public String getPayload() {
        return payload;
    }
}
