package pitiupi.control;

import pitiupi.GUI.MainWindow;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.plugin.Plugin;
import pitiupi.plugin.PrivateConnectionPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PluginManager {
    private final PluginLoader pluginLoader;
    private final List<Plugin> plugins;
    private final HashSet<String> loadedJars;

    public PluginManager(MainWindow main) {
        this.pluginLoader = new PluginLoader(main);
        this.plugins = new ArrayList<>();
        this.loadedJars = new HashSet<>();
    }

    public void loadPlugins(ConnectionManager connectionManager, GuiManager guiManager) {
        pluginLoader.loadPlugins(plugins, loadedJars, connectionManager, guiManager);
    }

    public void handlePublicMessage(byte[] msg) {
        for (Plugin plugin : plugins) {
            plugin.receiveMessage(msg);
        }
    }

    private Plugin getPlugin(String pluginId) {
        for (Plugin plugin : plugins) {
            if (plugin.getIdentifier().equals(pluginId)) return plugin;
        }
        return null;
    }

    public void handlePrivateMessage(TcpConnection connection, byte[] msg) {
        Plugin plugin = getPlugin(connection.getReceiver());
        if (plugin == null) return;

        if (plugin instanceof PrivateConnectionPlugin privatePlugin) {
            privatePlugin.receiveBytes(connection, msg);
        }

    }

    public boolean handleConnection(TcpConnection connection, String receiverId) {
        Plugin plugin = getPlugin(receiverId);
        if (plugin == null) return false;

        if (plugin instanceof PrivateConnectionPlugin privatePlugin) {
            privatePlugin.receivePrivateConnection(connection);
            return true;
        }

        return false;
    }

    public void handleDisconnection(TcpConnection connection, String receiverId) {
        Plugin plugin = getPlugin(receiverId);
        if (plugin instanceof PrivateConnectionPlugin privatePlugin) {
            privatePlugin.onSocketClosed(connection);
        }
    }
}
