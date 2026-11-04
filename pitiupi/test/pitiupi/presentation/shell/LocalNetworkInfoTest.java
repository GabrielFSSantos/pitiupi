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
    void classifiesWiredAndWirelessNames() {
        assertTrue(LocalNetworkInfo.isWiredInterface("eth0", null));
        assertTrue(LocalNetworkInfo.isWiredInterface("enp3s0", null));
        assertTrue(LocalNetworkInfo.isWiredInterface("net0", "Ethernet"));
        assertTrue(LocalNetworkInfo.isWirelessInterface("wlan0", null));
        assertTrue(LocalNetworkInfo.isWirelessInterface("wlp2s0", null));
        assertTrue(LocalNetworkInfo.isWirelessInterface("wlan2", "Wi-Fi"));
        assertFalse(LocalNetworkInfo.isWiredInterface("wlan0", "Wi-Fi"));
        assertFalse(LocalNetworkInfo.isWirelessInterface("eth0", "Ethernet"));
    }

    @Test
    void isLocalIpv4DetectsLoopback() {
        assertTrue(LocalNetworkInfo.isLocalIpv4("127.0.0.1"));
        assertTrue(LocalNetworkInfo.isLocalIpv4(LocalNetworkInfo.bestLocalIpv4()));
    }
}
