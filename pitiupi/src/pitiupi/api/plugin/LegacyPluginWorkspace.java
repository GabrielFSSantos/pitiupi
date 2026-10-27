package pitiupi.api.plugin;

import pitiupi.application.invites.InviteInboxService;
import pitiupi.application.peeractions.PeerActionBroker;
import pitiupi.application.peeractions.PluginPeerActionsRegistrar;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.models.Plugin;

import javax.swing.JComponent;

/**
 * Adaptador Onda C: expõe workspace e mantém {@link GuiManager} para plugins legados.
 */
public final class LegacyPluginWorkspace implements PluginWorkspace {

    private final String pluginId;
    private final ConnectionManager connectionManager;
    private final GuiManager guiManager;
    private final PeerActionBroker peerActionBroker;
    private final InviteInboxService inviteInboxService;
    private final Plugin plugin;
    private final PluginPeerActionsRegistrar registrar;

    public LegacyPluginWorkspace(
            String pluginId,
            Plugin plugin,
            ConnectionManager connectionManager,
            GuiManager guiManager,
            PeerActionBroker peerActionBroker,
            InviteInboxService inviteInboxService
    ) {
        this.pluginId = pluginId;
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.guiManager = guiManager;
        this.peerActionBroker = peerActionBroker;
        this.inviteInboxService = inviteInboxService;
        this.registrar = new PluginPeerActionsRegistrar(peerActionBroker, pluginId);
        peerActionBroker.registerPlugin(pluginId);
    }

    public GuiManager guiManager() {
        return guiManager;
    }

    public String pluginId() {
        return pluginId;
    }

    @Override
    public ConnectionManager connectionManager() {
        return connectionManager;
    }

    @Override
    public PeerActionsRegistrar peerActions() {
        return registrar;
    }

    @Override
    public void openOrFocusTab(String pluginInstanceId, String title, JComponent content) {
        peerActionBroker.setActivePlugin(pluginInstanceId);
    }

    @Override
    public void setStatusMessage(String text) {
    }

    @Override
    public void deliverInviteAccepted(InviteDetails invite) {
        plugin.onInviteAccepted(invite);
    }

    public void bindInviteDelivery() {
        inviteInboxService.setInviteHandler(pluginId, plugin::onInviteAccepted);
    }
}
