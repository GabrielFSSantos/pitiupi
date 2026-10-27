package pitiupi.presentation.shell;

import org.junit.jupiter.api.Test;
import pitiupi.control.ConnectionManager;
import pitiupi.control.plugins.PluginManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalUserIdentityTest {

    @Test
    void isLocalSenderMatchesUserNameAndLoopback() {
        ConnectionManager cm = new ConnectionManager("alice", new PluginManager(), 5000);
        assertTrue(LocalUserIdentity.isLocalSender(cm, "alice", "127.0.0.1"));
        assertTrue(LocalUserIdentity.isLocalSender(cm, "alice", LocalNetworkInfo.bestLocalIpv4()));
        assertFalse(LocalUserIdentity.isLocalSender(cm, "bob", "127.0.0.1"));
        assertFalse(LocalUserIdentity.isLocalSender(cm, "alice", "8.8.8.8"));
    }
}
