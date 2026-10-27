package pitiupi.plugins.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pitiupi.errors.PitiupiException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginStoreTest {

    @TempDir
    Path temp;

    private Path pluginsDir;
    private Path cacheDir;
    private PluginStore store;

    @BeforeEach
    void setUp() throws Exception {
        pluginsDir = temp.resolve("plugins");
        cacheDir = temp.resolve("cache");
        Files.createDirectories(pluginsDir);
        Files.createDirectories(cacheDir);
        store = new PluginStore(pluginsDir, cacheDir);
    }

    @Test
    void installCopiesJar() throws Exception {
        Path source = temp.resolve("sample.jar");
        Files.writeString(source, "fake-jar");

        Path installed = store.installFromFile(source);
        assertTrue(Files.isRegularFile(installed));
        assertEquals("sample.jar", installed.getFileName().toString());

        List<InstalledPluginRef> list = store.listInstalled();
        assertEquals(1, list.size());
    }

    @Test
    void removeRejectsPathOutsidePlugins() {
        Path outside = temp.resolve("evil.jar");
        assertThrows(PitiupiException.class, () -> store.remove(outside));
    }

    @Test
    void installRejectsNonJar() {
        Path txt = temp.resolve("not-a-jar.txt");
        assertThrows(PitiupiException.class, () -> {
            try {
                Files.writeString(txt, "x");
                store.installFromFile(txt);
            } catch (PitiupiException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
