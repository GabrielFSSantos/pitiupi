package pitiupi.api.plugin;

import pitiupi.application.invites.InviteInboxService;
import pitiupi.application.peeractions.PeerActionBroker;
import pitiupi.application.peeractions.PluginPeerActionsRegistrar;
import pitiupi.control.ConnectionManager;
import pitiupi.models.Plugin;
import pitiupi.presentation.shell.ShellHost;
import pitiupi.presentation.shell.TabWorkspacePanel;

import javax.swing.JComponent;

/**
 * Workspace com abas e barra de status.
 */
public final class ShellPluginWorkspace implements PluginWorkspace {

    private final String pluginId;
    private final Plugin plugin;
    private final ConnectionManager connectionManager;
    private final TabWorkspacePanel tabWorkspace;
    private final ShellHost shellHost;
    private final PluginPeerActionsRegistrar registrar;

    public ShellPluginWorkspace(
            String pluginId,
            Plugin plugin,
            ConnectionManager connectionManager,
            PeerActionBroker peerActionBroker,
            InviteInboxService inviteInboxService,
            TabWorkspacePanel tabWorkspace,
            ShellHost shellHost
    ) {
        this.pluginId = pluginId;
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.tabWorkspace = tabWorkspace;
        this.shellHost = shellHost;
        this.registrar = new PluginPeerActionsRegistrar(peerActionBroker, pluginId);
        peerActionBroker.registerPlugin(pluginId);
        peerActionBroker.registerPluginMetadata(pluginId, plugin.getDisplayName());
        inviteInboxService.setInviteHandler(pluginId, plugin::onInviteAccepted);
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
    public void openOrFocusTab(String pluginInstanceId) {
        openOrFocusTab(pluginInstanceId, plugin.getDisplayName(), null);
    }

    @Override
    public void openOrFocusTab(String pluginInstanceId, String title, JComponent content) {
        JComponent panel = content != null ? content : plugin.getShellWorkspaceContent();
        tabWorkspace.openOrFocusTab(pluginInstanceId, title, panel);
        connectionManager.getPeerActionBroker().setActivePlugin(pluginInstanceId);
    }

    @Override
    public void setStatusMessage(String text) {
        shellHost.setStatusMessage(text);
    }

    @Override
    public void deliverInviteAccepted(InviteDetails invite) {
        plugin.onInviteAccepted(invite);
    }
}
