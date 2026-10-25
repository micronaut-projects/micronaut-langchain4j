package io.micronaut.langchain4j.tools;

import io.micronaut.core.type.Argument;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The coercion of the JSON arguments of a tool follows LangChain4j's {@code DefaultToolExecutor}.
 */
class ToolArgumentsTest {

    @Test
    void argumentsAsMap() {
        assertEquals(Map.of(), ToolArguments.argumentsAsMap(null));
        assertEquals(Map.of(), ToolArguments.argumentsAsMap("  "));
        assertEquals(Map.of("a", 1), ToolArguments.argumentsAsMap("{\"a\": 1}"));
        // models quote the JSON, escape its quotes or leave trailing commas
        assertEquals(Map.of("a", "b"), ToolArguments.argumentsAsMap("\"{\\\"a\\\": \\\"b\\\"}\""));
        assertEquals(Map.of("a", List.of(1)), ToolArguments.argumentsAsMap("{\"a\": [1,],}"));
    }

    @Test
    void stringsEnumsAndBooleans() {
        assertEquals("1", coerce(1, String.class));
        assertEquals(Unit.CELSIUS, coerce("CELSIUS", Unit.class));
        assertEquals(Unit.CELSIUS, coerce(" celsius ", Unit.class));
        assertThrows(IllegalArgumentException.class, () -> coerce("kelvin", Unit.class));
        assertEquals(true, coerce(true, boolean.class));
        assertEquals(false, coerce(false, Boolean.class));
        assertThrows(IllegalArgumentException.class, () -> coerce("true", boolean.class));
    }

    @Test
    void numbers() {
        assertEquals(1.5d, coerce("1.5", double.class));
        assertEquals(2d, coerce(2, Double.class));
        assertThrows(IllegalArgumentException.class, () -> coerce("x", double.class));
        assertEquals(1.5f, coerce(1.5, float.class));
        assertThrows(IllegalArgumentException.class, () -> coerce(Double.MAX_VALUE, Float.class));
        assertEquals(new BigDecimal("1.25"), coerce(" 1.25 ", BigDecimal.class));
        assertEquals(new BigDecimal("3"), coerce(new BigInteger("3"), BigDecimal.class));
        assertEquals(new BigDecimal("4"), coerce(new BigDecimal("4"), BigDecimal.class));
        assertThrows(IllegalArgumentException.class, () -> coerce("x", BigDecimal.class));
        assertEquals(3, coerce("3", int.class));
        assertEquals(3, coerce(3.0, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> coerce(3.5, int.class));
        assertThrows(IllegalArgumentException.class, () -> coerce(Long.MAX_VALUE, int.class));
        assertEquals(9007199254740993L, coerce("9007199254740993", long.class));
        assertEquals((short) 7, coerce(7, short.class));
        assertEquals((byte) 7, coerce(7, Byte.class));
        assertThrows(IllegalArgumentException.class, () -> coerce(300, byte.class));
        assertEquals(new BigInteger("12345678901234567890"), coerce("12345678901234567890", BigInteger.class));
    }

    @Test
    void collectionsUuidsAndObjects() {
        assertEquals(List.of(1, 2), ToolArguments.coerceArgument(List.of(1, 2), "p", List.class, Argument.listOf(Integer.class).asType()));
        assertEquals(Map.of("k", "v"), ToolArguments.coerceArgument(Map.of("k", "v"), "p", Map.class, Argument.mapOf(String.class, String.class).asType()));
        UUID uuid = UUID.randomUUID();
        assertEquals(uuid, coerce(uuid.toString(), UUID.class));
        assertEquals(new Point(1, 2), coerce(Map.of("x", 1, "y", 2), Point.class));
        assertEquals(new Point(3, 4), coerce("{\"x\": 3, \"y\": 4}", Point.class));
    }

    @Test
    void defaultValues() {
        assertEquals("text", ToolArguments.parseDefaultValue("text", "p", String.class, String.class));
        assertEquals(Unit.FAHRENHEIT, ToolArguments.parseDefaultValue("fahrenheit", "p", Unit.class, Unit.class));
        assertEquals(5, ToolArguments.parseDefaultValue("5", "p", int.class, int.class));
        assertEquals(List.of("a"), ToolArguments.parseDefaultValue("[\"a\"]", "p", List.class, Argument.listOf(String.class).asType()));
        IllegalArgumentException invalid = assertThrows(IllegalArgumentException.class,
            () -> ToolArguments.parseDefaultValue("{", "p", int.class, int.class));
        assertTrue(invalid.getMessage().contains("Cannot parse @P(defaultValue"), invalid.getMessage());
        assertThrows(IllegalArgumentException.class, () -> ToolArguments.parseDefaultValue("null", "p", Integer.class, Integer.class));
    }

    private static Object coerce(Object value, Class<?> type) {
        return ToolArguments.coerceArgument(value, "p", type, type);
    }

    enum Unit {
        CELSIUS, FAHRENHEIT
    }

    record Point(int x, int y) {
    }
}
