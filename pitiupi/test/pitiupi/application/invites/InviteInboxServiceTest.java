package pitiupi.application.invites;

import org.junit.jupiter.api.Test;
import pitiupi.api.plugin.InviteDetails;
import pitiupi.application.presence.PresenceService;
import pitiupi.control.HeartbeatManager;
import pitiupi.models.Messages.HeartbeatMessage;
import pitiupi.models.Messages.SessionInviteMessage;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InviteInboxServiceTest {

    @Test
    void inviteIncrementsNotificationOnSender() throws Exception {
        HeartbeatManager heartbeat = HeartbeatManager.createForTesting();
        InviteInboxService inbox = new InviteInboxService(heartbeat);
        PresenceService presence = new PresenceService(heartbeat, inbox);

        heartbeat.receiveHeartbeat(new HeartbeatMessage("bob", "peer-bob"), InetAddress.getByName("127.0.0.1"));

        SessionInviteMessage invite = new SessionInviteMessage(
                "inv-1",
                "peer-alice",
                "Alice",
                heartbeat.getInstanceId(),
                "pitiupi.chat",
                SessionInviteKind.CHAT,
                "Oi"
        );
        inbox.receive(invite);

        assertEquals(1, inbox.getNotificationCount("peer-alice"));
        var views = presence.listPeerViews();
        var bob = views.stream().filter(v -> "peer-bob".equals(v.getInstanceId())).findFirst();
        assertTrue(bob.isEmpty() || bob.get().getNotificationCount() == 0);
    }

    @Test
    void acceptInviteDispatchesHandler() {
        HeartbeatManager heartbeat = HeartbeatManager.createForTesting();
        InviteInboxService inbox = new InviteInboxService(heartbeat);
        AtomicReference<InviteDetails> captured = new AtomicReference<>();

        inbox.setInviteHandler("pitiupi.chat", captured::set);

        SessionInviteMessage invite = new SessionInviteMessage(
                "inv-2",
                "peer-alice",
                "Alice",
                "",
                "pitiupi.chat",
                SessionInviteKind.CHAT,
                "teste"
        );
        inbox.receive(invite);
        assertTrue(inbox.acceptInvite("inv-2"));
        assertEquals("inv-2", captured.get().getInviteId());
        assertEquals(0, inbox.getNotificationCount("peer-alice"));
    }
}
