package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.annotation.processing.test.JavaParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class Langchain4jConfigVisitorTest {

    @Test
    void optionalInjectedPropertyGeneratesStoredFieldAndNullableGetter() {
        String generatedSource = generateConfigurationSource("""
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

        assertTrue(generatedSource.contains("TestCredentials credentials;"));
        assertTrue(generatedSource.contains("public TestCredentials getCredentials()"));
        assertTrue(generatedSource.contains("this.credentials = credentials;"));
        assertTrue(generatedSource.contains("this.builder.credentials(credentials);"));
        assertTrue(generatedSource.contains("@Nullable"));
    }

    @Test
    void requiredInjectedPropertyGeneratesStoredFieldAndGetterWithoutNullable() {
        String generatedSource = generateConfigurationSource("""
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

        assertTrue(generatedSource.contains("TestCredentials credentials;"));
        assertTrue(generatedSource.contains("public TestCredentials getCredentials()"));
        assertTrue(generatedSource.contains("this.credentials = credentials;"));
        assertTrue(generatedSource.contains("this.builder.credentials(credentials);"));
        assertFalse(generatedSource.contains("@Nullable"));
    }

    private static String generateConfigurationSource(String source) {
        try (JavaParser parser = new JavaParser()) {
            Iterable<? extends JavaFileObject> generated = parser.generate("test.TestModule", source);
            return StreamSupport.stream(generated.spliterator(), false)
                .filter(file -> file.getName().endsWith("DefaultTestModelConfiguration.java"))
                .findFirst()
                .map(Langchain4jConfigVisitorTest::readSource)
                .orElseThrow(() -> new AssertionError("Expected generated configuration source"));
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
