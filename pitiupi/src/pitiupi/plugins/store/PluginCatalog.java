package pitiupi.plugins.store;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê e grava {@code plugins.registry.json} na raiz do clone.
 * Entradas {@code build: jar} apontam para uma cópia estável em {@code .pitiupi/jar-catalog}.
 */
public final class PluginCatalog {

    public static final String BUILD_JAR = "jar";
    public static final String CATALOG_DIR = ".pitiupi/jar-catalog";
    public static final String DEFAULT_CATEGORY = "Social";

    private static final Pattern OBJECT = Pattern.compile("\\{[^{}]*}");
    private static final Pattern FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");

    private PluginCatalog() {
    }

    public record Entry(String id, String dir, String build, String jar, String menuCategory) {
    }

    /**
     * O catálogo fica ao lado de {@code .pitiupi}, pai da raiz de dados.
     */
    public static Optional<Path> locateFromHome(Path homeRoot) {
        if (homeRoot == null) {
            return Optional.empty();
        }
        Path parent = homeRoot.toAbsolutePath().normalize().getParent();
        if (parent == null) {
            return Optional.empty();
        }
        Path registry = parent.resolve("plugins.registry.json");
        if (Files.isRegularFile(registry)) {
            return Optional.of(registry);
        }
        return Optional.empty();
    }

    public static List<Entry> read(Path registry) throws IOException {
        String text = Files.readString(registry);
        List<Entry> entries = new ArrayList<>();
        Matcher objects = OBJECT.matcher(text);
        while (objects.find()) {
            String body = objects.group();
            String id = field(body, "id");
            String dir = field(body, "dir");
            String build = field(body, "build");
            String jar = field(body, "jar");
            String category = field(body, "menuCategory");
            if (id == null || dir == null || build == null || jar == null || category == null) {
                continue;
            }
            entries.add(new Entry(id, dir, build, jar, category));
        }
        return entries;
    }

    public static void write(Path registry, List<Entry> entries) throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"plugins\": [\n");
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            out.append("    {\n");
            out.append("      \"id\": \"").append(escape(entry.id())).append("\",\n");
            out.append("      \"dir\": \"").append(escape(entry.dir())).append("\",\n");
            out.append("      \"build\": \"").append(escape(entry.build())).append("\",\n");
            out.append("      \"jar\": \"").append(escape(entry.jar())).append("\",\n");
            out.append("      \"menuCategory\": \"").append(escape(entry.menuCategory())).append("\"\n");
            out.append("    }");
            if (i + 1 < entries.size()) {
                out.append(',');
            }
            out.append('\n');
        }
        out.append("  ]\n}\n");
        Files.writeString(registry, out.toString());
    }

    public static void upsert(Path registry, Entry entry) throws IOException {
        List<Entry> entries = read(registry);
        boolean replaced = false;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).id().equals(entry.id())) {
                entries.set(i, entry);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            entries.add(entry);
        }
        write(registry, entries);
    }

    public static void removeMatchingJar(Path registry, String fileName) throws IOException {
        if (registry == null || fileName == null || !Files.isRegularFile(registry)) {
            return;
        }
        String stem = stemOf(fileName);
        List<Entry> kept = new ArrayList<>();
        for (Entry entry : read(registry)) {
            if (!matches(entry, fileName, stem)) {
                kept.add(entry);
            }
        }
        write(registry, kept);
    }

    public static boolean matches(Entry entry, String fileName, String stem) {
        if (entry.id().equals(stem)) {
            return true;
        }
        if (BUILD_JAR.equals(entry.build())) {
            String jarName = Path.of(entry.jar()).getFileName().toString();
            return jarName.equals(fileName);
        }
        return false;
    }

    public static String stemOf(String fileName) {
        String name = fileName;
        if (name.endsWith(".jar")) {
            name = name.substring(0, name.length() - 4);
        }
        String cleaned = name.replaceAll("[^A-Za-z0-9._-]", "");
        if (cleaned.isBlank()) {
            return "plugin";
        }
        return cleaned;
    }

    public static Path catalogDirectory(Path registry) {
        return registry.getParent().resolve(CATALOG_DIR);
    }

    private static String field(String objectBody, String name) {
        Matcher matcher = FIELD.matcher(objectBody);
        while (matcher.find()) {
            if (name.equals(matcher.group(1))) {
                return matcher.group(2).replace("\\\"", "\"").replace("\\\\", "\\");
            }
        }
        return null;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
