package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.annotation.processing.test.JavaParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class Langchain4jConfigVisitorTest {

    @Test
    void optionalInjectedPropertyGeneratesStoredFieldAndNullableGetter() {
        Map<String, String> generatedSources = generateConfigurationSources("""
            package test;

            import io.micronaut.langchain4j.annotation.Lang4jConfig;
            import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
            import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;

            @Lang4jConfig(
                models = @Model(kind = TestKind.class, impl = TestModel.class),
                properties = @Property(name = "credentials", injected = true)
            )
            final class TestModule {
            }

            interface TestKind {
            }

            final class TestCredentials {
            }

            final class TestModel implements TestKind {
                static Builder builder() {
                    return new Builder();
                }

                static final class Builder {
                    Builder credentials(TestCredentials credentials) {
                        return this;
                    }

                    TestModel build() {
                        return new TestModel();
                    }
                }
            }
            """);

        assertGeneratedConfiguration(
            generatedSources,
            "DefaultTestModelConfiguration.java",
            generatedSource -> assertNullableCredentialsMembers(generatedSource)
        );
        assertGeneratedConfiguration(
            generatedSources,
            "NamedTestModelConfiguration.java",
            generatedSource -> assertNullableCredentialsMembers(generatedSource)
        );
    }

    @Test
    void requiredInjectedPropertyGeneratesStoredFieldAndGetterWithoutNullable() {
        Map<String, String> generatedSources = generateConfigurationSources("""
            package test;

            import io.micronaut.langchain4j.annotation.Lang4jConfig;
            import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
            import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;

            @Lang4jConfig(
                models = @Model(kind = TestKind.class, impl = TestModel.class),
                properties = @Property(name = "credentials", injected = true, required = true)
            )
            final class TestModule {
            }

            interface TestKind {
            }

            final class TestCredentials {
            }

            final class TestModel implements TestKind {
                static Builder builder() {
                    return new Builder();
                }

                static final class Builder {
                    Builder credentials(TestCredentials credentials) {
                        return this;
                    }

                    TestModel build() {
                        return new TestModel();
                    }
                }
            }
            """);

        assertGeneratedConfiguration(
            generatedSources,
            "DefaultTestModelConfiguration.java",
            generatedSource -> assertRequiredCredentialsMembers(generatedSource)
        );
        assertGeneratedConfiguration(
            generatedSources,
            "NamedTestModelConfiguration.java",
            generatedSource -> assertRequiredCredentialsMembers(generatedSource)
        );
    }

    private static void assertNullableCredentialsMembers(String generatedSource) {
        assertMatches(generatedSource, "@Nullable\\s+TestCredentials credentials;");
        assertMatches(generatedSource, "@Nullable\\s+public TestCredentials getCredentials\\(\\)");
        assertMatches(generatedSource, "protected void credentials\\(@Nullable TestCredentials credentials\\)");
        assertTrue(generatedSource.contains("this.credentials = credentials;"));
        assertTrue(generatedSource.contains("this.builder.credentials(credentials);"));
    }

    private static void assertRequiredCredentialsMembers(String generatedSource) {
        assertMatches(generatedSource, "(?m)^\\s*TestCredentials credentials;$");
        assertMatches(generatedSource, "(?m)^\\s*public TestCredentials getCredentials\\(\\) \\{$");
        assertMatches(generatedSource, "(?m)^\\s*protected void credentials\\(TestCredentials credentials\\) \\{$");
        assertTrue(generatedSource.contains("this.credentials = credentials;"));
        assertTrue(generatedSource.contains("this.builder.credentials(credentials);"));
        assertFalse(Pattern.compile("@Nullable\\s+TestCredentials credentials;").matcher(generatedSource).find());
        assertFalse(Pattern.compile("@Nullable\\s+public TestCredentials getCredentials\\(\\)").matcher(generatedSource).find());
        assertFalse(Pattern.compile("credentials\\(@Nullable TestCredentials credentials\\)").matcher(generatedSource).find());
    }

    private static void assertGeneratedConfiguration(
        Map<String, String> generatedSources,
        String fileName,
        Consumer<String> assertions) {
        assertions.accept(generatedSources.getOrDefault(fileName, ""));
    }

    private static void assertMatches(String generatedSource, String regex) {
        assertTrue(Pattern.compile(regex, Pattern.MULTILINE | Pattern.DOTALL).matcher(generatedSource).find(), generatedSource);
    }

    private static Map<String, String> generateConfigurationSources(String source) {
        try (JavaParser parser = new JavaParser()) {
            Iterable<? extends JavaFileObject> generated = parser.generate("test.TestModule", source);
            return StreamSupport.stream(generated.spliterator(), false)
                .filter(file -> file.getName().endsWith("DefaultTestModelConfiguration.java")
                    || file.getName().endsWith("NamedTestModelConfiguration.java"))
                .collect(java.util.stream.Collectors.toMap(
                    file -> file.getName().substring(file.getName().lastIndexOf('/') + 1),
                    Langchain4jConfigVisitorTest::readSource
                ));
        }
    }

    private static String readSource(JavaFileObject file) {
        try {
            return file.getCharContent(true).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
