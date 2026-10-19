/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.tools;

import dev.langchain4j.internal.Json;
import io.micronaut.core.util.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Parses the JSON arguments of a tool execution request and coerces them to the parameter types of the tool
 * method, with the rules of LangChain4j's {@code DefaultToolExecutor} and {@code ToolExecutionRequestUtil}, whose
 * helpers are package-private.
 */
final class ToolArguments {

    private static final Pattern TRAILING_COMMA_PATTERN = Pattern.compile(",(\\s*[}\\]])");
    private static final Pattern LEADING_TRAILING_QUOTE_PATTERN = Pattern.compile("(^\")|(\"$)");
    private static final Pattern ESCAPED_QUOTE_PATTERN = Pattern.compile("\\\\\"");
    private static final Type MAP_TYPE = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[] {String.class, Object.class};
        }

        @Override
        public Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };

    private ToolArguments() {
    }

    /**
     * @param arguments The JSON arguments of the request
     * @return The arguments by name
     */
    static Map<String, Object> argumentsAsMap(String arguments) {
        if (StringUtils.isEmpty(arguments) || arguments.isBlank()) {
            return Map.of();
        }
        try {
            return Json.fromJson(arguments, MAP_TYPE);
        } catch (Exception ignored) {
            String normalized = ESCAPED_QUOTE_PATTERN.matcher(LEADING_TRAILING_QUOTE_PATTERN.matcher(arguments).replaceAll(""))
                .replaceAll("\"");
            return Json.fromJson(TRAILING_COMMA_PATTERN.matcher(normalized).replaceAll("$1"), MAP_TYPE);
        }
    }

    /**
     * Parses the {@code @P(defaultValue)} of a parameter.
     *
     * @param defaultValue   The default value
     * @param parameterName  The parameter name
     * @param parameterClass The parameter class
     * @param parameterType  The generic parameter type
     * @return The value
     */
    static Object parseDefaultValue(String defaultValue, String parameterName, Class<?> parameterClass, Type parameterType) {
        if (parameterClass == String.class || parameterClass.isEnum() || parameterClass == UUID.class) {
            return coerceArgument(defaultValue, parameterName, parameterClass, parameterType);
        }
        Object jsonParsed;
        try {
            jsonParsed = Json.fromJson(defaultValue, Object.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot parse @P(defaultValue = \"%s\") for parameter \"%s\" of type %s: %s"
                    .formatted(defaultValue, parameterName, parameterClass.getName(), e.getMessage()), e);
        }
        if (jsonParsed == null) {
            throw new IllegalArgumentException("@P(defaultValue = \"%s\") parses to null for parameter \"%s\" of type %s"
                .formatted(defaultValue, parameterName, parameterClass.getName()));
        }
        return coerceArgument(jsonParsed, parameterName, parameterClass, parameterType);
    }

    /**
     * Coerces a JSON value to the type of a parameter.
     *
     * @param argument       The JSON value
     * @param parameterName  The parameter name
     * @param parameterClass The parameter class
     * @param parameterType  The generic parameter type
     * @return The value
     */
    @SuppressWarnings({"unchecked", "rawtypes", "java:S3776"})
    static Object coerceArgument(Object argument, String parameterName, Class<?> parameterClass, Type parameterType) {
        if (parameterClass == String.class) {
            return argument.toString();
        }
        if (parameterClass.isEnum()) {
            try {
                Class<Enum> enumClass = (Class<Enum>) parameterClass;
                String enumValue = Objects.requireNonNull(argument).toString().strip();
                try {
                    return Enum.valueOf(enumClass, enumValue);
                } catch (IllegalArgumentException e) {
                    return Enum.valueOf(enumClass, enumValue.toUpperCase(Locale.ROOT));
                }
            } catch (Exception | Error e) {
                throw new IllegalArgumentException("Argument \"%s\" is not a valid enum value for %s: <%s>"
                    .formatted(parameterName, parameterClass.getName(), argument), e);
            }
        }
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            if (argument instanceof Boolean) {
                return argument;
            }
            throw notConvertable(argument, parameterName, parameterClass);
        }
        if (parameterClass == Double.class || parameterClass == double.class) {
            return doubleValue(argument, parameterName, parameterClass);
        }
        if (parameterClass == Float.class || parameterClass == float.class) {
            double value = doubleValue(argument, parameterName, parameterClass);
            if (value < -Float.MAX_VALUE || value > Float.MAX_VALUE) {
                throw new IllegalArgumentException("Argument \"%s\" is out of range for %s: <%s>"
                    .formatted(parameterName, parameterClass.getName(), value));
            }
            return (float) value;
        }
        if (parameterClass == BigDecimal.class) {
            return bigDecimalValue(argument, parameterName, parameterClass);
        }
        if (parameterClass == Integer.class || parameterClass == int.class) {
            return (int) boundedLongValue(argument, parameterName, parameterClass, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }
        if (parameterClass == Long.class || parameterClass == long.class) {
            return boundedLongValue(argument, parameterName, parameterClass, Long.MIN_VALUE, Long.MAX_VALUE);
        }
        if (parameterClass == Short.class || parameterClass == short.class) {
            return (short) boundedLongValue(argument, parameterName, parameterClass, Short.MIN_VALUE, Short.MAX_VALUE);
        }
        if (parameterClass == Byte.class || parameterClass == byte.class) {
            return (byte) boundedLongValue(argument, parameterName, parameterClass, Byte.MIN_VALUE, Byte.MAX_VALUE);
        }
        if (parameterClass == BigInteger.class) {
            return bigIntegerValue(argument, parameterName, parameterClass);
        }
        if (Collection.class.isAssignableFrom(parameterClass) || Map.class.isAssignableFrom(parameterClass)) {
            return Json.fromJson(Json.toJson(argument), parameterType);
        }
        if (parameterClass == UUID.class) {
            return UUID.fromString(argument.toString());
        }
        if (argument instanceof String) {
            return Json.fromJson(argument.toString(), parameterClass);
        }
        return Json.fromJson(Json.toJson(argument), parameterClass);
    }

    private static double doubleValue(Object argument, String parameterName, Class<?> parameterClass) {
        if (argument instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                // reported below
            }
        }
        if (!(argument instanceof Number number)) {
            throw notConvertable(argument, parameterName, parameterClass);
        }
        return number.doubleValue();
    }

    private static long boundedLongValue(Object argument, String parameterName, Class<?> parameterClass, long minValue, long maxValue) {
        BigInteger value = bigIntegerValue(argument, parameterName, parameterClass);
        if (value.compareTo(BigInteger.valueOf(minValue)) < 0 || value.compareTo(BigInteger.valueOf(maxValue)) > 0) {
            throw new IllegalArgumentException("Argument \"%s\" is out of range for %s: <%s>"
                .formatted(parameterName, parameterClass.getName(), argument));
        }
        return value.longValue();
    }

    private static BigInteger bigIntegerValue(Object argument, String parameterName, Class<?> parameterClass) {
        try {
            return bigDecimalValue(argument, parameterName, parameterClass).toBigIntegerExact();
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Argument \"%s\" has non-integer value for %s: <%s>"
                .formatted(parameterName, parameterClass.getName(), argument));
        }
    }

    private static BigDecimal bigDecimalValue(Object argument, String parameterName, Class<?> parameterClass) {
        if (argument instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (argument instanceof BigInteger bigInteger) {
            return new BigDecimal(bigInteger);
        }
        if (argument instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (argument instanceof String string) {
            try {
                return new BigDecimal(string.trim());
            } catch (NumberFormatException e) {
                // reported below
            }
        }
        throw notConvertable(argument, parameterName, parameterClass);
    }

    private static IllegalArgumentException notConvertable(Object argument, String parameterName, Class<?> parameterClass) {
        return new IllegalArgumentException("Argument \"%s\" is not convertible to %s, got %s: <%s>".formatted(
            parameterName, parameterClass.getName(), argument == null ? "null" : argument.getClass().getName(), argument));
    }
}
