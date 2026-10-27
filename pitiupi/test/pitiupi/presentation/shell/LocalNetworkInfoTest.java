package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalNetworkInfoTest {

    @Test
    void bestLocalIpv4ReturnsNonEmpty() {
        String ip = LocalNetworkInfo.bestLocalIpv4();
        assertNotNull(ip);
        assertFalse(ip.isBlank());
    }

    @Test
    void isLocalIpv4DetectsLoopback() {
        assertTrue(LocalNetworkInfo.isLocalIpv4("127.0.0.1"));
        assertTrue(LocalNetworkInfo.isLocalIpv4(LocalNetworkInfo.bestLocalIpv4()));
    }
}
