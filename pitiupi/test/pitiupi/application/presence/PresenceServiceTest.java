package pitiupi.application.presence;

import org.junit.jupiter.api.Test;
import pitiupi.control.HeartbeatManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PresenceServiceTest {

    @Test
    void listPeerViewsStartsWithBroadcastTarget() {
        PresenceService service = new PresenceService(HeartbeatManager.createForTesting());
        var views = service.listPeerViews();
        assertTrue(views.get(0).isBroadcastTarget());
        assertEquals("Todos", views.get(0).getDisplayName());
    }
}
