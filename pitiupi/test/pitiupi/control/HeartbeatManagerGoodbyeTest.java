package pitiupi.control;

import org.junit.jupiter.api.Test;
import pitiupi.models.Messages.GoodbyeMessage;
import pitiupi.models.Messages.HeartbeatMessage;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartbeatManagerGoodbyeTest {

    @Test
    void receiveGoodbyeRemovesPeer() throws Exception {
        HeartbeatManager manager = HeartbeatManager.createForTesting();
        InetAddress addr = InetAddress.getByName("127.0.0.1");

        manager.receiveHeartbeat(new HeartbeatMessage("alice", "peer-1"), addr);
        assertEquals(1, manager.getPeers().size());

        manager.receiveGoodbye(new GoodbyeMessage("alice", "peer-1"));
        assertTrue(manager.getPeers().isEmpty());
    }
}
