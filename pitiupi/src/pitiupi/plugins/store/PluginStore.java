package pitiupi.plugins.store;

import pitiupi.errors.ErrorCode;
import pitiupi.errors.PitiupiException;
import pitiupi.platform.PitiupiHome;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Instalação e remoção local de JARs em {@code PITIUPI_HOME/plugins}.
 */
public final class PluginStore {

    private final Path pluginsDir;
    private final Path cacheDir;

    public PluginStore() {
        PitiupiHome home = PitiupiHome.get();
        this.pluginsDir = home.pluginsDirectory();
        this.cacheDir = home.cacheDirectory();
    }

    public PluginStore(Path pluginsDir, Path cacheDir) {
        this.pluginsDir = pluginsDir.normalize().toAbsolutePath();
        this.cacheDir = cacheDir.normalize().toAbsolutePath();
    }

    public List<InstalledPluginRef> listInstalled() throws IOException {
        if (!Files.isDirectory(pluginsDir)) {
            return List.of();
        }
        List<InstalledPluginRef> result = new ArrayList<>();
        try (Stream<Path> stream = Files.list(pluginsDir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".jar"))
                    .forEach(jar -> result.add(toRef(jar)));
        }
        return result;
    }

    public Path installFromFile(Path sourceJar) throws PitiupiException, IOException {
        if (sourceJar == null || !Files.isRegularFile(sourceJar)) {
            throw new PitiupiException(ErrorCode.INVALID_PATH);
        }
        String fileName = sourceJar.getFileName().toString();
        if (!fileName.endsWith(".jar") || fileName.contains("..")) {
            throw new PitiupiException(ErrorCode.INVALID_PATH);
        }

        Files.createDirectories(pluginsDir);
        Files.createDirectories(cacheDir);

        Path dest = pluginsDir.resolve(fileName).normalize();
        if (!dest.startsWith(pluginsDir.normalize().toAbsolutePath())) {
            throw new PitiupiException(ErrorCode.INVALID_PATH);
        }

        Path tmp = cacheDir.resolve(fileName + "." + UUID.randomUUID() + ".part");
        try {
            Files.copy(sourceJar, tmp, StandardCopyOption.REPLACE_EXISTING);
            Files.move(tmp, dest, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return dest;
        } catch (IOException e) {
            try {
                Files.deleteIfExists(tmp);
            } catch (IOException ignored) {
                // best effort
            }
            throw new PitiupiException(ErrorCode.PLUGIN_INSTALL_FAILED, e);
        }
    }

    public void remove(Path jarInPluginsDir) throws PitiupiException, IOException {
        Path normalized = jarInPluginsDir.normalize().toAbsolutePath();
        Path pluginsAbs = pluginsDir.normalize().toAbsolutePath();
        if (!normalized.startsWith(pluginsAbs) || !normalized.getFileName().toString().endsWith(".jar")) {
            throw new PitiupiException(ErrorCode.INVALID_PATH);
        }
        try {
            Files.deleteIfExists(normalized);
            Files.deleteIfExists(PluginMetadataWriter.metaPathFor(normalized));
        } catch (IOException e) {
            throw new PitiupiException(ErrorCode.PLUGIN_REMOVE_FAILED, e);
        }
    }

    public boolean exists(String arquivoId) throws IOException {
        return findJarByArquivoId(arquivoId).isPresent();
    }

    public Optional<Path> findJarByArquivoId(String id) throws IOException {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        for (InstalledPluginRef ref : listInstalled()) {
            if (ref.getArquivoId().map(id::equals).orElse(false)) {
                return Optional.of(ref.getJarPath());
            }
        }
        return Optional.empty();
    }

    private InstalledPluginRef toRef(Path jar) {
        Properties meta = loadMeta(PluginMetadataWriter.metaPathFor(jar));
        String fileName = jar.getFileName().toString();
        return new InstalledPluginRef(
                jar,
                fileName,
                optional(meta, "arquivoId"),
                optional(meta, "version"),
                optional(meta, "name"),
                optional(meta, "displayName"),
                optional(meta, "identifier"),
                optional(meta, "author")
        );
    }

    private static Optional<String> optional(Properties meta, String key) {
        String v = meta.getProperty(key);
        if (v == null || v.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(v.trim());
    }

    private Properties loadMeta(Path metaPath) {
        Properties props = new Properties();
        if (!Files.isRegularFile(metaPath)) {
            return props;
        }
        try (InputStream in = Files.newInputStream(metaPath)) {
            props.load(in);
        } catch (IOException ignored) {
            // listagem tolerante
        }
        return props;
    }
}
