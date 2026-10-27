package pitiupi.platform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PitiupiHomeTest {

    @Test
    void ensureLayoutCreatesPluginsAndCache(@TempDir Path temp) throws Exception {
        PitiupiHome home = PitiupiHome.open(temp.resolve("pitiupi-data"));
        home.ensureLayout();
        assertTrue(Files.isDirectory(home.pluginsDirectory()));
        assertTrue(Files.isDirectory(home.cacheDirectory()));
        assertTrue(Files.isDirectory(home.logsDirectory()));
        assertTrue(Files.isRegularFile(home.configFile()));
    }

    @Test
    void pluginsDirectoryUnderRoot(@TempDir Path temp) {
        Path root = temp.resolve("custom-home");
        PitiupiHome home = PitiupiHome.open(root);
        assertTrue(home.pluginsDirectory().startsWith(home.rootDirectory()));
    }
}
