package pitiupi.presentation.shell;

import pitiupi.api.plugin.ShellPluginWorkspace;
import pitiupi.control.ConnectionManager;
import pitiupi.control.GuiManager;
import pitiupi.control.plugins.PluginManager;
import pitiupi.models.Plugin;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Janela principal pós-login (shell com abas).
 */
public final class MainShellFrame extends JFrame implements ShellHost {

    private final PluginManager pluginManager = new PluginManager();
    private final ConnectionManager connectionManager;
    private final NetworkUsersPanel networkUsersPanel;
    private final GuiManager guiManager;
    private final TabWorkspacePanel tabWorkspace;
    private final PluginGalleryPanel galleryPanel;
    private final SettingsPanel settingsPanel;
    private final JLabel statusLabel = new JLabel(" ");

    public MainShellFrame(String userName, int port) {
        setTitle("Pitiupi");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                exit();
            }
        });

        connectionManager = new ConnectionManager(userName, pluginManager, port);
        settingsPanel = new SettingsPanel(connectionManager);
        PluginGalleryPanel gallery = new PluginGalleryPanel((pluginId, title) -> { });
        tabWorkspace = new TabWorkspacePanel(gallery, settingsPanel);
        galleryPanel = gallery;
        galleryPanel.setOpenHandler(this::openPluginTabFromGallery);

        networkUsersPanel = new NetworkUsersPanel(connectionManager);
        ShellMenuBar menuBar = new ShellMenuBar();
        setJMenuBar(menuBar);

        guiManager = new GuiManager(this, menuBar, networkUsersPanel);

        JPanel center = new JPanel(new BorderLayout());
        center.add(tabWorkspace.getRootPanel(), BorderLayout.CENTER);
        getContentPane().add(center, BorderLayout.CENTER);
        getContentPane().add(networkUsersPanel, BorderLayout.EAST);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        tabWorkspace.setOnActivePluginChanged(activeId -> {
            if (activeId != null) {
                connectionManager.getPeerActionBroker().setActivePlugin(activeId);
            }
            for (Plugin plugin : pluginManager.getLoadedPlugins()) {
                String pluginId = plugin.getIdentifier();
                if (activeId == null || !activeId.equals(pluginId)) {
                    connectionManager.getPeerActivityFocus().clear(pluginId);
                }
            }
        });
        tabWorkspace.setOnSettingsTabSelected(settingsPanel::onTabSelected);
        tabWorkspace.setOnPluginTabClosed(pluginManager::notifyShellTabClosed);
        connectionManager.setInviteAcceptedUiListener(this::focusPluginTab);

        loadPlugins();
        updateStatus(userName, port);
        setLocationByPlatform(false);
        UiGeometry.installCenterOnShow(this);
        pack();
        setSize(1024, 768);
    }

    public void showOnScreen() {
        UiGeometry.centerOnScreen(this);
        setVisible(true);
        javax.swing.SwingUtilities.invokeLater(() -> UiGeometry.centerOnScreen(this));
    }

    private void loadPlugins() {
        pluginManager.loadPlugins(connectionManager, guiManager, (plugin) -> new ShellPluginWorkspace(
                plugin.getIdentifier(),
                plugin,
                connectionManager,
                guiManager,
                connectionManager.getPeerActionBroker(),
                connectionManager.getInviteInboxService(),
                tabWorkspace,
                this
        ));
        List<Plugin> plugins = pluginManager.getLoadedPlugins();
        galleryPanel.setPlugins(plugins);
        settingsPanel.setLoadedPlugins(plugins);
    }

    private void updateStatus(String userName, int port) {
        setStatusMessage("Conectado na rede · porta " + port + " · você: " + userName);
    }

    private void openPluginTabFromGallery(String pluginId, String title) {
        JComponent content = pluginManager.getLoadedPlugins().stream()
                .filter(p -> p.getIdentifier().equals(pluginId))
                .map(Plugin::getShellWorkspaceContent)
                .findFirst()
                .orElse(null);
        tabWorkspace.openOrFocusTab(pluginId, title, content);
        connectionManager.getPeerActionBroker().setActivePlugin(pluginId);
    }

    private void focusPluginTab(String pluginId) {
        String title = pluginManager.getLoadedPlugins().stream()
                .filter(p -> p.getIdentifier().equals(pluginId))
                .map(Plugin::getDisplayName)
                .findFirst()
                .orElse(pluginId);
        openPluginTabFromGallery(pluginId, title);
    }

    @Override
    public void exit() {
        connectionManager.shutdownGracefully();
        setVisible(false);
        dispose();
        System.exit(0);
    }

    @Override
    public void setUserName(String userName) {
        connectionManager.setUserName(userName);
        updateStatus(userName, connectionManager.getPort());
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }
}
