package pitiupi.application.activity;

import org.junit.jupiter.api.Test;
import pitiupi.api.plugin.PeerActivityFocusTokens;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeerActivityFocusTest {

    @Test
    void focusClearsPendingForPluginChannel() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        PeerActivityFocus focus = new PeerActivityFocus();
        inbox.setFocus(focus);

        inbox.recordCounted("peer-1", "plugin.chat", "chat.peer", PeerActivityFocusTokens.peerInstance("peer-1"));
        assertEquals(1, inbox.totalCountedForPeer("peer-1"));

        focus.setFocusedChannel("plugin.chat", PeerActivityFocusTokens.peerInstance("peer-1"));
        assertEquals(0, inbox.totalCountedForPeer("peer-1"));
        assertTrue(focus.isFocused("plugin.chat", PeerActivityFocusTokens.peerInstance("peer-1")));
    }

    @Test
    void recordSkippedWhileFocused() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        PeerActivityFocus focus = new PeerActivityFocus();
        inbox.setFocus(focus);
        focus.setFocusedChannel("plugin.chat", PeerActivityFocusTokens.broadcast());

        inbox.recordBroadcastCounted("plugin.chat", "chat.focus", PeerActivityFocusTokens.broadcast());
        assertEquals(0, inbox.totalCountedForPeer(PeerActivityInbox.BROADCAST_PEER_KEY));
    }

    @Test
    void signalCapsAtOne() {
        PeerActivityInbox inbox = new PeerActivityInbox();
        inbox.signalBroadcast("plugin.cast", "screencast.multicast", PeerActivityFocusTokens.broadcast());
        inbox.signalBroadcast("plugin.cast", "screencast.multicast", PeerActivityFocusTokens.broadcast());
        assertTrue(inbox.hasIndicatorForPeer(PeerActivityInbox.BROADCAST_PEER_KEY));
        assertTrue(inbox.hasIndicatorForAction(
                PeerActivityInbox.BROADCAST_PEER_KEY, "plugin.cast", "screencast.multicast"));
    }
}
