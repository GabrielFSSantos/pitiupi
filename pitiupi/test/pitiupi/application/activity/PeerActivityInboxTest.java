package pitiupi.application.activity;

import org.junit.jupiter.api.Test;
import pitiupi.api.plugin.PeerActivityFocusTokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeerActivityInboxTest {

    @Test
    void recordsAndClearsCountedPerAction() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        inbox.recordCounted("peer-1", "plugin.chat", "chat.peer", null);
        inbox.recordCounted("peer-1", "plugin.chat", "chat.peer", null);
        assertEquals(2, inbox.countForAction("peer-1", "plugin.chat", "chat.peer"));
        assertEquals(2, inbox.totalCountedForPeer("peer-1"));
        inbox.clearAction("peer-1", "plugin.chat", "chat.peer");
        assertEquals(0, inbox.totalCountedForPeer("peer-1"));
    }

    @Test
    void signalIsIndicatorNotCounted() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        inbox.signal("peer-1", "plugin.cast", "screencast.peer", null);
        inbox.signal("peer-1", "plugin.cast", "screencast.peer", null);
        assertEquals(0, inbox.totalCountedForPeer("peer-1"));
        assertTrue(inbox.hasIndicatorForPeer("peer-1"));
    }
}
