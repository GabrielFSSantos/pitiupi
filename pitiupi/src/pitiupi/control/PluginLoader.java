/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pitiupi.control;

import java.awt.List;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.Vector;
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

    public void loadPlugins() {
        File pluginDir = new File("plugins");
        File[] jarFiles = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No plugins found in the " + pluginDir.getAbsolutePath() + " directory.");
            return;
        }

        try {
            // 2. Convert jar file paths into URLs
            ArrayList urls = new ArrayList();
            for (File file : jarFiles) {
                urls.add(file.toURI().toURL());
                System.out.println("Discovered plugin package: " + file.getName());
            }

            // 3. Create a class loader isolated from the application's default loader
            URLClassLoader pluginClassLoader = new URLClassLoader(
                    (URL[])urls.toArray(new URL[0]),
                    Main.class.getClassLoader()
            );
            System.out.println(pluginClassLoader);

            // 4. Locate and instantiate all classes implementing the Plugin interface
            ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);

            System.out.println("\n--- Executing Discovered Plugins ---" + serviceLoader.findFirst());
            for (Plugin plugin : serviceLoader) {
                System.out.println("Loading: " + plugin.getName());
                plugin.createPlugin(this.mainWindow);
                this.mainWindow.getPlugins().add(plugin);
            }

        } catch (MalformedURLException e) {
            System.out.println(e.toString());
        }
    }
}
