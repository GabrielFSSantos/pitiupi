package pitiupi.control;

import pitiupi.GUI.MainWindow;
import pitiupi.Main;
import pitiupi.net.tcp.TcpConnection;
import pitiupi.model.Plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;

public class PluginManager {
    private final List<Plugin> plugins;
    private final HashSet<String> loadedJars;

    public PluginManager() {
        this.plugins = new ArrayList<>();
        this.loadedJars = new HashSet<>();
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

        plugin.receiveBytes(connection, msg);
    }

    public boolean handleConnection(TcpConnection connection, String receiverId) {
        Plugin plugin = getPlugin(receiverId);
        if (plugin == null) return false;

        plugin.receivePrivateConnection(connection);

        return false;
    }

    public void handleDisconnection(TcpConnection connection, String receiverId) {
        Plugin plugin = getPlugin(receiverId);
        if (plugin == null) return;

        plugin.onSocketClosed(connection);
    }

    public void loadPlugins(ConnectionManager connectionManager, GuiManager guiManager) {
        File pluginDir = new File("plugins");
        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No plugins found in the " + pluginDir.getAbsolutePath() + " directory.");
            return;
        }

        for (File file : jarFiles) {

            if (loadedJars.contains(file.getName())) {
                continue;
            }

            System.out.println("Discovered plugin package: " + file.getName());

            try {
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{file.toURI().toURL()},
                        Main.class.getClassLoader()
                );

                ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);

                for (Plugin plugin : serviceLoader) {
                    System.out.println("Loading: " + plugin.getName());

                    plugin.createPlugin(connectionManager, guiManager);
                    plugins.add(plugin);
                    loadedJars.add(file.getName());
                }

            } catch (Exception e) {
                System.out.println("Failed to load plugin " + file.getName());
                e.printStackTrace();
            }
        }
    }
}
