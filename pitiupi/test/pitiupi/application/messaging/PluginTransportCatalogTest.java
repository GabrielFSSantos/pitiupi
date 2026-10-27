package pitiupi.application.messaging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginTransportCatalogTest {

    @Test
    void screenCastBroadcastExcludesTcp() {
        var modes = PluginTransportCatalog.forBroadcast(PluginTransportCatalog.PluginKind.SCREEN_CAST);
        assertTrue(modes.contains(TransportMode.MULTICAST_UDP));
        assertFalse(modes.contains(TransportMode.TCP));
    }
}
