package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.annotation.processing.test.JavaParser;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;
import io.micronaut.sourcegen.model.AnnotationDef;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @Test
    void createsDefaultNamedAnnotation() {
        AnnotationDef annotation = Langchain4jConfigVisitor.defaultNamedAnnotation();

        assertEquals(Named.class.getName(), annotationTypeName(annotation));
        assertMemberContains(annotation, "value", "default");
    }

    @Test
    void createsNamedDefaultPropertyRequirement() {
        AnnotationDef annotation = Langchain4jConfigVisitor.missingNamedDefaultPropertyRequirement("langchain4j.open-ai.chat-models");

        assertEquals(Requires.class.getName(), annotationTypeName(annotation));
        assertMemberContains(annotation, "missingProperty", "langchain4j.open-ai.chat-models.default");
    }

    @Test
    void createsPrimaryDefaultEachPropertyAnnotation() {
        AnnotationDef annotation = Langchain4jConfigVisitor.eachPropertyWithPrimaryDefaultAnnotation("langchain4j.open-ai.chat-models");

        assertEquals(EachProperty.class.getName(), annotationTypeName(annotation));
        assertMemberContains(annotation, "value", "langchain4j.open-ai.chat-models");
        assertMemberContains(annotation, "primary", "default");
    }

    private static void assertMemberContains(AnnotationDef annotation, String memberName, String expectedValue) {
        Object memberValue = annotationMembers(annotation).get(memberName);

        assertNotNull(memberValue, () -> "Missing annotation member: " + memberName);
        assertTrue(memberValue.toString().contains(expectedValue), () -> "Expected " + memberName + " to contain " + expectedValue + " but was " + memberValue);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> annotationMembers(AnnotationDef annotation) {
        return (Map<String, Object>) invokeZeroArg(annotation, "getValues", "getMembers");
    }

    private static String annotationTypeName(AnnotationDef annotation) {
        Object type = invokeZeroArg(annotation, "getType", "getAnnotationType");
        Object name = invokeZeroArg(type, "getName", "getCanonicalName");
        return name.toString();
    }

    private static Object invokeZeroArg(Object target, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
            } catch (NoSuchMethodException _) {
                // Try the next candidate.
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to invoke " + methodName + " on " + target.getClass().getName(), e);
            }
        }
        throw new IllegalStateException("No compatible zero-arg method found on " + target.getClass().getName());
    }
}
