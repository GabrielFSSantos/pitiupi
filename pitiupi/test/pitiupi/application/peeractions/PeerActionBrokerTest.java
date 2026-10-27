package pitiupi.application.peeractions;

import org.junit.jupiter.api.Test;
import pitiupi.application.presence.UserPeerView;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeerActionBrokerTest {

    @Test
    void exposesOnlyActivePluginActions() {
        PeerActionBroker broker = new PeerActionBroker();
        broker.registerPlugin("plugin.a");
        broker.registerPlugin("plugin.b");

        AtomicBoolean ran = new AtomicBoolean(false);
        broker.registerAction("plugin.a", PeerAction.of("a1", "Ação A", PeerActionTarget.SINGLE_PEER, t -> ran.set(true)));
        broker.registerAction("plugin.b", PeerAction.of("b1", "Ação B", PeerActionTarget.SINGLE_PEER, t -> {
        }));

        broker.setActivePlugin("plugin.b");
        assertEquals(1, broker.getActionsFor(PeerActionTarget.SINGLE_PEER).size());
        assertEquals("b1", broker.getActionsFor(PeerActionTarget.SINGLE_PEER).get(0).getId());

        broker.setActivePlugin("plugin.a");
        broker.getActionsFor(PeerActionTarget.SINGLE_PEER).get(0).getHandler()
                .run(new PeerTarget.Single(UserPeerView.broadcastTarget()));
        assertTrue(ran.get());
    }

    @Test
    void getAllActionsForGroupsByPlugin() {
        PeerActionBroker broker = new PeerActionBroker();
        broker.registerPlugin("plugin.a");
        broker.registerPlugin("plugin.b");
        broker.registerPluginMetadata("plugin.a", "Chat");
        broker.registerPluginMetadata("plugin.b", "Tela");

        broker.registerAction("plugin.a", PeerAction.of("a1", "Abrir", PeerActionTarget.ALL_PEERS, t -> {
        }));
        broker.registerAction("plugin.b", PeerAction.of("b1", "Focar", PeerActionTarget.ALL_PEERS, t -> {
        }));
        broker.registerAction("plugin.b", PeerAction.of("b2", "Privado", PeerActionTarget.SINGLE_PEER, t -> {
        }));

        assertEquals(2, broker.getAllActionsFor(PeerActionTarget.ALL_PEERS).size());
        assertEquals("Chat", broker.getAllActionsFor(PeerActionTarget.ALL_PEERS).get(0).pluginDisplayName());
        assertEquals(1, broker.getAllActionsFor(PeerActionTarget.SINGLE_PEER).size());
        assertEquals("Tela", broker.getAllActionsFor(PeerActionTarget.SINGLE_PEER).get(0).pluginDisplayName());
    }
}
