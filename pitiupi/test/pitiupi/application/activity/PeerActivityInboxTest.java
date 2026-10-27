package pitiupi.application.activity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerActivityInboxTest {

    @Test
    void recordsAndClearsPerAction() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        inbox.record("peer-1", "plugin.chat", "chat.peer");
        inbox.record("peer-1", "plugin.chat", "chat.peer");
        assertEquals(2, inbox.countForAction("peer-1", "plugin.chat", "chat.peer"));
        assertEquals(2, inbox.totalForPeer("peer-1"));
        inbox.clearAction("peer-1", "plugin.chat", "chat.peer");
        assertEquals(0, inbox.totalForPeer("peer-1"));
    }
}
