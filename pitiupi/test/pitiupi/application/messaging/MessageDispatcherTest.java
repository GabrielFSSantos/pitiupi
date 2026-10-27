package pitiupi.application.messaging;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageDispatcherTest {

    @Test
    void sendTargetFactories() throws Exception {
        assertEquals(SendTarget.BroadcastAll.class, SendTarget.broadcastAll().getClass());
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        assertEquals(addr, ((SendTarget.SinglePeer) SendTarget.singlePeer(addr)).address());
    }

    @Test
    void transportModeLabels() {
        assertEquals("UDP multicast", TransportMode.MULTICAST_UDP.getLabel());
        assertEquals("UDP unicast", TransportMode.UNICAST_UDP.getLabel());
        assertTrue(TransportMode.MULTICAST_UDP.describeForUi(true).contains("(UDP multicast)"));
    }
}
