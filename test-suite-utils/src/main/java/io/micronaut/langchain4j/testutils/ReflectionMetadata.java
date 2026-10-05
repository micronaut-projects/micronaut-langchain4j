package io.micronaut.langchain4j.testutils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Verifies the GraalVM reachability metadata a provider module ships for the LangChain4j types that are mapped with
 * Jackson reflectively (the request and response types of the provider's REST API).
 *
 * <p>The expected types are the classes of the given packages of the LangChain4j jar, except the model classes (the
 * implementations of LangChain4j's model interfaces) and their builders, the helpers (HTTP clients, mappers, parsers...),
 * the exceptions and the anonymous classes.
 * When LangChain4j adds a type, the verification fails and names it. Run the test with the environment variable
 * {@value #UPDATE} set to rewrite the metadata file.</p>
 */
public final class ReflectionMetadata {

    /**
     * The environment variable that rewrites the metadata file instead of verifying it.
     */
    public static final String UPDATE = "UPDATE_NATIVE_METADATA";

    private static final Set<String> MODEL_INTERFACES = Set.of(
        "dev.langchain4j.model.chat.ChatModel",
        "dev.langchain4j.model.chat.StreamingChatModel",
        "dev.langchain4j.model.language.LanguageModel",
        "dev.langchain4j.model.language.StreamingLanguageModel",
        "dev.langchain4j.model.embedding.EmbeddingModel",
        "dev.langchain4j.model.image.ImageModel",
        "dev.langchain4j.model.moderation.ModerationModel",
        "dev.langchain4j.model.scoring.ScoringModel"
    );

    // helpers of the providers (HTTP clients, mappers, parsers...) that LangChain4j never maps with Jackson
    private static final Pattern HELPER = Pattern.compile(
        ".*(Client|Utils|Helper|Parser|ResponseBuilder|Mapper|Service|Processor|Writer|Writers|Estimator|Catalog|BaseChatModel|Batch(Chat|Embedding|Image)Model)|Base.*ChatModel|Json");

    private static final Pattern TYPE = Pattern.compile("\"type\"\\s*:\\s*\"([^\"]+)\"");

    private ReflectionMetadata() {
    }

    /**
     * Verifies, or rewrites, the metadata file of a module.
     *
     * @param module   The module name, for example {@code micronaut-langchain4j-ollama}
     * @param anchor   A class of the LangChain4j jar that contains the packages
     * @param packages The packages of the request and response types
     */
    public static void verify(String module, Class<?> anchor, String... packages) {
        Path file = Path.of("src/main/resources/META-INF/native-image/io.micronaut.langchain4j", module, "reachability-metadata.json");
        Set<String> expected = expectedTypes(anchor, Set.of(packages));
        if (System.getenv(UPDATE) != null) {
            write(file, expected);
            return;
        }
        Set<String> actual = declaredTypes(file);
        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        Set<String> extra = new TreeSet<>(actual);
        extra.removeAll(expected);
        if (!missing.isEmpty() || !extra.isEmpty()) {
            throw new AssertionError("The native image metadata " + file + " is out of date."
                + "\nMissing types: " + missing
                + "\nTypes that no longer exist or are not expected: " + extra
                + "\nRerun the test with the environment variable " + UPDATE + "=true to rewrite it.");
        }
    }

    static Set<String> expectedTypes(Class<?> anchor, Set<String> packages) {
        ClassLoader classLoader = anchor.getClassLoader();
        Set<String> types = new TreeSet<>();
        for (String className : classNames(anchor)) {
            int lastDot = className.lastIndexOf('.');
            if (!packages.contains(className.substring(0, lastDot)) || className.matches(".*\\$\\d+.*")) {
                continue;
            }
            try {
                Class<?> type = Class.forName(className, false, classLoader);
                if (!isExcluded(type)) {
                    types.add(className);
                }
            } catch (ClassNotFoundException | LinkageError e) {
                // a type of an optional dependency
            }
        }
        return types;
    }

    private static boolean isExcluded(Class<?> type) {
        Class<?> topLevel = type;
        while (topLevel.getEnclosingClass() != null) {
            topLevel = topLevel.getEnclosingClass();
        }
        if (Throwable.class.isAssignableFrom(type) || isModel(type) || HELPER.matcher(topLevel.getSimpleName()).matches()) {
            return true;
        }
        // the builders of the models
        Class<?> enclosing = type.getEnclosingClass();
        return enclosing != null && isModel(enclosing);
    }

    private static boolean isModel(Class<?> type) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            if (implementsModel(current)) {
                return true;
            }
        }
        return false;
    }

    private static boolean implementsModel(Class<?> type) {
        for (Class<?> anInterface : type.getInterfaces()) {
            if (MODEL_INTERFACES.contains(anInterface.getName()) || implementsModel(anInterface)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> classNames(Class<?> anchor) {
        try {
            Path location = Path.of(anchor.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (Files.isDirectory(location)) {
                try (Stream<Path> files = Files.walk(location)) {
                    return files.map(location::relativize)
                        .map(path -> path.toString().replace(location.getFileSystem().getSeparator(), "/"))
                        .filter(name -> name.endsWith(".class"))
                        .map(ReflectionMetadata::toClassName)
                        .toList();
                }
            }
            try (JarFile jar = new JarFile(location.toFile())) {
                List<String> names = new ArrayList<>();
                jar.stream()
                    .map(entry -> entry.getName())
                    .filter(name -> name.endsWith(".class") && !name.startsWith("META-INF/"))
                    .map(ReflectionMetadata::toClassName)
                    .forEach(names::add);
                return names;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toClassName(String resourceName) {
        return resourceName.substring(0, resourceName.length() - ".class".length()).replace('/', '.');
    }

    private static Set<String> declaredTypes(Path file) {
        if (!Files.exists(file)) {
            return Set.of();
        }
        try {
            Matcher matcher = TYPE.matcher(Files.readString(file));
            Set<String> types = new TreeSet<>();
            while (matcher.find()) {
                types.add(matcher.group(1));
            }
            return types;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(Path file, Collection<String> types) {
        String entries = types.stream()
            .map(type -> "    {\"type\": \"" + type + "\", \"allDeclaredConstructors\": true, \"allPublicConstructors\": true, "
                + "\"allDeclaredMethods\": true, \"allPublicMethods\": true, \"allDeclaredFields\": true, \"allPublicFields\": true}")
            .collect(Collectors.joining(",\n"));
        String json = "{\n  \"reflection\": [\n" + entries + "\n  ]\n}\n";
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
