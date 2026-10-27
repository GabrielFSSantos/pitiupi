package pitiupi.net.tcp;

import org.junit.jupiter.api.Test;

class TcpConnectionManagerTest {

    @Test
    void closeAllConnectionsOnEmptyManagerDoesNotThrow() {
        TcpConnectionManager manager = new TcpConnectionManager();
        manager.closeAllConnections();
        manager.closeAllConnections("reason");
    }
}
