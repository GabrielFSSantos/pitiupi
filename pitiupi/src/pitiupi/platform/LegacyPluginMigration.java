package pitiupi.platform;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Migração única de {@code ./plugins} (CWD legado) para {@code PITIUPI_HOME/plugins}.
 */
public final class LegacyPluginMigration {

    private LegacyPluginMigration() {
    }

    public static void maybeMigrateFromLegacyCwd() throws IOException {
        PitiupiHome home = PitiupiHome.get();
        ConfigLoader config = home.config();

        if (config.isMigrationCwdPluginsDone()) {
            return;
        }

        Path legacyDir = Path.of(System.getProperty("user.dir"), "plugins").normalize();
        Path targetDir = home.pluginsDirectory();

        boolean legacyHasJars = hasJarFiles(legacyDir);
        boolean targetEmpty = !hasJarFiles(targetDir);

        if (!legacyHasJars || !targetEmpty) {
            config.setMigrationCwdPluginsDone(true);
            return;
        }

        boolean copy = false;
        if (GraphicsEnvironmentHelper.isHeadless() || !config.isMigrationCwdPluginsPromptEnabled()) {
            copy = false;
        } else {
            int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Encontramos plugins na pasta antiga (" + legacyDir + ").\n"
                            + "Copiar para " + targetDir + "?",
                    "Migração de plugins",
                    JOptionPane.YES_NO_OPTION
            );
            copy = choice == JOptionPane.YES_OPTION;
        }

        if (copy) {
            copyJarsAndMeta(legacyDir, targetDir);
        } else {
            config.setMigrationCwdPluginsPromptEnabled(false);
        }

        config.setMigrationCwdPluginsDone(true);
    }

    private static boolean hasJarFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return false;
        }
        try (Stream<Path> s = Files.list(dir)) {
            return s.anyMatch(p -> p.getFileName().toString().endsWith(".jar"));
        }
    }

    private static void copyJarsAndMeta(Path from, Path to) throws IOException {
        Files.createDirectories(to);
        try (Stream<Path> s = Files.list(from)) {
            s.filter(p -> {
                String name = p.getFileName().toString();
                return name.endsWith(".jar") || name.endsWith(".plugin.meta");
            }).forEach(src -> {
                try {
                    Files.copy(src, to.resolve(src.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * Evita referência direta a java.awt.GraphicsEnvironment em testes headless.
     */
    static final class GraphicsEnvironmentHelper {
        static boolean isHeadless() {
            return java.awt.GraphicsEnvironment.isHeadless();
        }
    }
}
