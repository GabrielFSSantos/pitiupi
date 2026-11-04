package pitiupi.plugins.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginCatalogTest {

    @TempDir
    Path temp;

    @Test
    void upsertKeepsOtherEntriesAndRemoveDropsOnlyTheMatch() throws Exception {
        Path registry = temp.resolve("plugins.registry.json");
        Files.writeString(registry, """
                {
                  "plugins": [
                    {
                      "id": "chat",
                      "dir": "plugins/chat",
                      "build": "ant",
                      "jar": "dist/pitiupi.chat.jar",
                      "menuCategory": "Social"
                    },
                    {
                      "id": "screencast",
                      "dir": "plugins/screencast",
                      "build": "ant",
                      "jar": "dist/pitiupi.screenCast.jar",
                      "menuCategory": "Streaming"
                    }
                  ]
                }
                """);

        PluginCatalog.upsert(registry, new PluginCatalog.Entry(
                "extra",
                PluginCatalog.CATALOG_DIR,
                PluginCatalog.BUILD_JAR,
                "extra.jar",
                PluginCatalog.DEFAULT_CATEGORY
        ));

        List<PluginCatalog.Entry> afterAdd = PluginCatalog.read(registry);
        assertEquals(3, afterAdd.size());
        assertEquals("chat", afterAdd.get(0).id());
        assertEquals("ant", afterAdd.get(0).build());
        assertEquals("extra", afterAdd.get(2).id());
        assertEquals(PluginCatalog.BUILD_JAR, afterAdd.get(2).build());

        PluginCatalog.removeMatchingJar(registry, "extra.jar");
        List<PluginCatalog.Entry> afterRemove = PluginCatalog.read(registry);
        assertEquals(2, afterRemove.size());
        assertEquals("chat", afterRemove.get(0).id());
        assertEquals("screencast", afterRemove.get(1).id());

        PluginCatalog.removeMatchingJar(registry, "chat.jar");
        List<PluginCatalog.Entry> afterChat = PluginCatalog.read(registry);
        assertEquals(1, afterChat.size());
        assertEquals("screencast", afterChat.get(0).id());
    }

    @Test
    void locateFromHomeFindsRegistryBesideDataDir() throws Exception {
        Path registry = temp.resolve("plugins.registry.json");
        Files.writeString(registry, "{ \"plugins\": [] }\n");
        Path home = temp.resolve(".pitiupi");
        Files.createDirectories(home);
        assertTrue(PluginCatalog.locateFromHome(home).isPresent());
        assertEquals(registry, PluginCatalog.locateFromHome(home).orElseThrow());
        Path unrelated = temp.resolve("sem-catalogo").resolve("dados");
        Files.createDirectories(unrelated);
        assertFalse(PluginCatalog.locateFromHome(unrelated).isPresent());
    }
}
