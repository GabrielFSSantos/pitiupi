package pitiupi.application.peeractions;

import pitiupi.api.plugin.PeerActionsRegistrar;

public final class PluginPeerActionsRegistrar implements PeerActionsRegistrar {

    private final PeerActionBroker broker;
    private final String pluginId;

    public PluginPeerActionsRegistrar(PeerActionBroker broker, String pluginId) {
        this.broker = broker;
        this.pluginId = pluginId;
    }

    @Override
    public void register(PeerAction action) {
        broker.registerAction(pluginId, action);
    }
}
