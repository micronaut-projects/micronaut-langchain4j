package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;
import io.micronaut.sourcegen.model.AnnotationDef;
import jakarta.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Langchain4jConfigVisitorTest {

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
            } catch (NoSuchMethodException ignored) {
                // Try the next candidate.
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Unable to invoke " + methodName + " on " + target.getClass().getName(), e);
            }
        }
        throw new IllegalStateException("No compatible zero-arg method found on " + target.getClass().getName());
    }
}
