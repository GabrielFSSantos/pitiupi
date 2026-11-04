package pitiupi.net.tcp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TcpConnectNoticeTest {

    @Test
    void existingSocketDoesNotAnnounceConnecting() {
        assertFalse(TcpConnectNotice.shouldShowConnecting(true));
        assertTrue(TcpConnectNotice.shouldShowConnecting(false));
    }
}
