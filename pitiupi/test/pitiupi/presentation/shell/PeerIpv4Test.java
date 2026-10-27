package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PeerIpv4Test {

    @Test
    void mapsIpv4MappedIpv6ToDottedDecimal() throws Exception {
        InetAddress mapped = InetAddress.getByName("0:0:0:0:0:ffff:a9fe:f8c4");
        assertEquals("169.254.248.196", PeerIpv4.fromInetAddress(mapped));
    }

    @Test
    void stripsZoneIdFromString() {
        assertEquals("169.254.248.196", PeerIpv4.normalize("0:0:0:0:0:ffff:a9fe:f8c4%7"));
    }

    @Test
    void keepsPlainIpv4() throws Exception {
        InetAddress v4 = InetAddress.getByName("169.254.165.248");
        assertEquals("169.254.165.248", PeerIpv4.fromInetAddress(v4));
    }
}
