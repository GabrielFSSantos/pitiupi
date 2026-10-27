package pitiupi.platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Raiz de dados do usuário ({@code PITIUPI_HOME} ou {@code ~/pitiupi}).
 */
public final class PitiupiHome {

    private static final PitiupiHome DEFAULT = new PitiupiHome(resolveRoot());

    private final Path root;
    private ConfigLoader configLoader;

    private PitiupiHome(Path root) {
        this.root = root.normalize().toAbsolutePath();
    }

    public static PitiupiHome get() {
        return DEFAULT;
    }

    /**
     * Instância com raiz explícita (testes e ferramentas).
     */
    public static PitiupiHome open(Path root) {
        return new PitiupiHome(root);
    }

    public static Path resolveRoot() {
        String env = System.getenv("PITIUPI_HOME");
        if (env != null && !env.isBlank()) {
            return expandUserHome(env.trim());
        }
        return Paths.get(System.getProperty("user.home"), "pitiupi");
    }

    private static Path expandUserHome(String path) {
        if (path.startsWith("~/")) {
            return Paths.get(System.getProperty("user.home"), path.substring(2));
        }
        if ("~".equals(path)) {
            return Paths.get(System.getProperty("user.home"));
        }
        return Paths.get(path);
    }

    public Path rootDirectory() {
        return root;
    }

    public Path pluginsDirectory() {
        return root.resolve("plugins");
    }

    public Path cacheDirectory() {
        return root.resolve("cache");
    }

    public Path logsDirectory() {
        return root.resolve("logs");
    }

    public Path configFile() {
        return root.resolve("config.properties");
    }

    public ConfigLoader config() throws IOException {
        if (configLoader == null) {
            configLoader = new ConfigLoader(root);
            configLoader.loadOrCreateDefaults();
        }
        return configLoader;
    }

    /**
     * Cria diretórios padrão se ausentes.
     */
    public void ensureLayout() throws IOException {
        Files.createDirectories(pluginsDirectory());
        Files.createDirectories(cacheDirectory());
        Files.createDirectories(logsDirectory());
        config();
    }
}
