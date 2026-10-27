package pitiupi.api.plugin;

import pitiupi.control.ConnectionManager;

import javax.swing.JComponent;

/**
 * Superfície exposta ao plugin na aba central (evolução da API legada).
 */
public interface PluginWorkspace {

    ConnectionManager connectionManager();

    PeerActionsRegistrar peerActions();

    void openOrFocusTab(String pluginInstanceId, String title, JComponent content);

    default void openOrFocusTab(String pluginId) {
        openOrFocusTab(pluginId, pluginId, null);
    }

    default void openOrFocusTab(String pluginInstanceId, String title) {
        openOrFocusTab(pluginInstanceId, title, null);
    }

    void setStatusMessage(String text);

    void deliverInviteAccepted(InviteDetails invite);
}
