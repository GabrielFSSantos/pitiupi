package pitiupi.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigLoaderTest {

    @Test
    void loadOrCreateDefaultsWritesConfig(@TempDir Path home) throws Exception {
        ConfigLoader loader = new ConfigLoader(home);
        loader.loadOrCreateDefaults();
        assertTrue(Files.isRegularFile(home.resolve("config.properties")));
        assertTrue(loader.isMigrationCwdPluginsPromptEnabled());
        assertFalse(loader.isMigrationCwdPluginsDone());
    }

    @Test
    void setMigrationFlagsPersist(@TempDir Path home) throws Exception {
        ConfigLoader loader = new ConfigLoader(home);
        loader.loadOrCreateDefaults();
        loader.setMigrationCwdPluginsDone(true);
        ConfigLoader reloaded = new ConfigLoader(home);
        reloaded.loadOrCreateDefaults();
        assertTrue(reloaded.isMigrationCwdPluginsDone());
    }
}
