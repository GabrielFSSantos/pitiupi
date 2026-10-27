package pitiupi.application.presence;

import pitiupi.application.activity.PeerActivityInbox;
import pitiupi.application.invites.InviteInboxService;
import pitiupi.control.HeartbeatManager;
import pitiupi.models.Messages.GoodbyeMessage;
import pitiupi.models.Messages.HeartbeatMessage;
import pitiupi.models.User;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra presença na camada de aplicação (sem Swing).
 */
public final class PresenceService {

    private final HeartbeatManager heartbeatManager;
    private final InviteInboxService inviteInboxService;
    private final PeerActivityInbox peerActivityInbox;

    public PresenceService(HeartbeatManager heartbeatManager) {
        this(heartbeatManager, null, null);
    }

    public PresenceService(HeartbeatManager heartbeatManager, InviteInboxService inviteInboxService) {
        this(heartbeatManager, inviteInboxService, null);
    }

    public PresenceService(
            HeartbeatManager heartbeatManager,
            InviteInboxService inviteInboxService,
            PeerActivityInbox peerActivityInbox
    ) {
        this.heartbeatManager = heartbeatManager;
        this.inviteInboxService = inviteInboxService;
        this.peerActivityInbox = peerActivityInbox;
    }

    public void onHeartbeat(HeartbeatMessage msg, InetAddress from) {
        heartbeatManager.receiveHeartbeat(msg, from);
    }

    public void onGoodbye(GoodbyeMessage msg) {
        heartbeatManager.receiveGoodbye(msg);
    }

    public List<UserPeerView> listPeerViews() {
        List<UserPeerView> views = new ArrayList<>();
        int broadcastActivity = peerActivityInbox != null
                ? peerActivityInbox.totalForPeer(PeerActivityInbox.BROADCAST_PEER_KEY)
                : 0;
        views.add(new UserPeerView("Todos", null, null, broadcastActivity, false, true));
        String selfId = heartbeatManager.getInstanceId();
        for (User user : heartbeatManager.getPeers()) {
            boolean isSelf = selfId.equals(user.getInstanceId());
            int notifications = 0;
            if (inviteInboxService != null && user.getInstanceId() != null) {
                notifications += inviteInboxService.getNotificationCount(user.getInstanceId());
            }
            if (peerActivityInbox != null && user.getInstanceId() != null) {
                notifications += peerActivityInbox.totalForPeer(user.getInstanceId());
            }
            views.add(new UserPeerView(
                    user.getUserName(),
                    user.getInstanceId(),
                    user.getAddress(),
                    notifications,
                    isSelf,
                    false
            ));
        }
        return views;
    }

    public UserPeerView broadcastTarget() {
        return UserPeerView.broadcastTarget();
    }
}
