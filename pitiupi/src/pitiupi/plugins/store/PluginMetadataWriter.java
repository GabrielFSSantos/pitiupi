package pitiupi.plugins.store;

import pitiupi.models.Plugin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Grava {@code *.plugin.meta} após carregar um plugin com sucesso.
 */
public final class PluginMetadataWriter {

    public void write(Plugin plugin, Path jarPath) throws IOException {
        Path metaPath = metaPathFor(jarPath);
        Properties props = new Properties();
        props.setProperty("identifier", plugin.getIdentifier());
        props.setProperty("name", plugin.getName());
        props.setProperty("displayName", plugin.getDisplayName());
        props.setProperty("author", plugin.getAuthor());
        props.setProperty("version", plugin.getVersion());
        String arquivoId = deriveArquivoId(jarPath, plugin);
        props.setProperty("arquivoId", arquivoId);

        try (OutputStream out = Files.newOutputStream(metaPath)) {
            props.store(out, "Pitiupi plugin metadata");
        }
    }

    static Path metaPathFor(Path jarPath) {
        return Path.of(jarPath.toString() + ".plugin.meta");
    }

    private static String deriveArquivoId(Path jarPath, Plugin plugin) {
        String file = jarPath.getFileName().toString();
        if (file.endsWith(".jar")) {
            String base = file.substring(0, file.length() - 4);
            if (base.startsWith("pitiupi.")) {
                return base.substring("pitiupi.".length());
            }
            return base;
        }
        return plugin.getIdentifier();
    }
}
