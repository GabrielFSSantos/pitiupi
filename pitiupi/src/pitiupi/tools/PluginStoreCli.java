package pitiupi.tools;

import pitiupi.errors.PitiupiException;
import pitiupi.platform.PitiupiHome;
import pitiupi.plugins.store.InstalledPluginRef;
import pitiupi.plugins.store.PluginStore;

import java.nio.file.Path;
import java.util.List;

/**
 * CLI mínima para smoke REQ-02 (instalar/listar/remover JAR) sem Swing.
 */
public final class PluginStoreCli {

    private PluginStoreCli() {
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            usage();
            System.exit(1);
        }
        try {
            PitiupiHome.get().ensureLayout();
            PluginStore store = new PluginStore();
            switch (args[0]) {
                case "install" -> install(store, args);
                case "list" -> list(store);
                case "remove" -> remove(store, args);
                default -> {
                    usage();
                    System.exit(1);
                }
            }
        } catch (PitiupiException ex) {
            System.err.println("ERRO: " + ex.getUserMessage());
            System.exit(2);
        } catch (Exception ex) {
            System.err.println("ERRO: " + ex.getMessage());
            System.exit(2);
        }
    }

    private static void install(PluginStore store, String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(1);
        }
        Path dest = store.installFromFile(Path.of(args[1]));
        System.out.println("OK install " + dest.getFileName());
    }

    private static void list(PluginStore store) throws Exception {
        List<InstalledPluginRef> refs = store.listInstalled();
        if (refs.isEmpty()) {
            System.out.println("(vazio)");
            return;
        }
        for (InstalledPluginRef ref : refs) {
            System.out.println(ref.getFileName() + "\t" + ref.getJarPath());
        }
    }

    private static void remove(PluginStore store, String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            System.exit(1);
        }
        Path jar = Path.of(args[1]);
        store.remove(jar);
        System.out.println("OK remove " + jar.getFileName());
    }

    private static void usage() {
        System.err.println("Uso: PluginStoreCli install <arquivo.jar> | list | remove <caminho-jar-em-plugins>");
    }
}
