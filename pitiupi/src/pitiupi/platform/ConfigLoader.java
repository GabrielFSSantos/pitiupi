package pitiupi.platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Leitura e gravação de {@code config.properties} na raiz do PITIUPI_HOME.
 */
public final class ConfigLoader {

    private final Path configFile;
    private final Properties properties = new Properties();

    public ConfigLoader(Path homeRoot) {
        this.configFile = homeRoot.resolve("config.properties");
    }

    public void loadOrCreateDefaults() throws IOException {
        if (Files.isRegularFile(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                properties.load(in);
            }
        } else {
            properties.setProperty("migration.cwdPluginsPrompt", "true");
            store();
        }
    }

    public void store() throws IOException {
        Files.createDirectories(configFile.getParent());
        try (OutputStream out = Files.newOutputStream(configFile)) {
            properties.store(out, "Pitiupi configuration");
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = properties.getProperty(key);
        if (v == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(v.trim());
    }

    public void setBoolean(String key, boolean value) throws IOException {
        properties.setProperty(key, value ? "true" : "false");
        store();
    }

    public boolean isMigrationCwdPluginsDone() {
        return getBoolean("migration.cwdPluginsDone", false);
    }

    public void setMigrationCwdPluginsDone(boolean done) throws IOException {
        setBoolean("migration.cwdPluginsDone", done);
    }

    public boolean isMigrationCwdPluginsPromptEnabled() {
        return getBoolean("migration.cwdPluginsPrompt", true);
    }

    public void setMigrationCwdPluginsPromptEnabled(boolean enabled) throws IOException {
        setBoolean("migration.cwdPluginsPrompt", enabled);
    }
}
