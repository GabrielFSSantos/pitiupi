package pitiupi.plugins.store;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Referência a um JAR instalado e metadados opcionais.
 */
public final class InstalledPluginRef {

    private final Path jarPath;
    private final String fileName;
    private final Optional<String> arquivoId;
    private final Optional<String> version;
    private final Optional<String> name;
    private final Optional<String> displayName;
    private final Optional<String> identifier;
    private final Optional<String> author;

    public InstalledPluginRef(
            Path jarPath,
            String fileName,
            Optional<String> arquivoId,
            Optional<String> version,
            Optional<String> name,
            Optional<String> displayName,
            Optional<String> identifier,
            Optional<String> author
    ) {
        this.jarPath = jarPath;
        this.fileName = fileName;
        this.arquivoId = arquivoId;
        this.version = version;
        this.name = name;
        this.displayName = displayName;
        this.identifier = identifier;
        this.author = author;
    }

    public Path getJarPath() {
        return jarPath;
    }

    public String getFileName() {
        return fileName;
    }

    public Optional<String> getArquivoId() {
        return arquivoId;
    }

    public Optional<String> getVersion() {
        return version;
    }

    public Optional<String> getName() {
        return name;
    }

    public Optional<String> getDisplayName() {
        return displayName;
    }

    public Optional<String> getIdentifier() {
        return identifier;
    }

    public Optional<String> getAuthor() {
        return author;
    }
}
