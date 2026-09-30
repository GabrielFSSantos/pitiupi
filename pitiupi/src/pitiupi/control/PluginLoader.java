/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.control;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import pitiupi.GUI.MainWindow;
import pitiupi.Main;
import pitiupi.plugin.Plugin;

/**
 * Responsável por localizar, carregar e registrar os plugins
 * presentes no diretório {@code plugins}.
 *
 * <p>Os plugins são descobertos utilizando {@link java.util.ServiceLoader}
 * e devem estar empacotados como arquivos JAR contendo a configuração
 * {@code META-INF/services} correspondente.</p>
 * @author flavio
 */
public class PluginLoader {

    private final pitiupi.GUI.MainWindow mainWindow;

    public PluginLoader(MainWindow main) {
        this.mainWindow = main;
    }

    public void loadPlugins(List<Plugin> plugins, HashSet<String> loadedJars, ConnectionManager connectionManager, GuiManager guiManager) {
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
