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
package io.micronaut.langchain4j.serde;

import org.jspecify.annotations.Nullable;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.Decoder;
import io.micronaut.serde.Deserializer;
import io.micronaut.serde.exceptions.SerdeException;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Deserializers of {@link LocalDate}, {@link LocalTime} and {@link LocalDateTime} accepting, besides their ISO
 * strings, the object shapes LangChain4j's codec accepts: the JSON schema LangChain4j derives for these types
 * describes the fields of the classes ({@code {"year", "month", "day"}}, {@code {"hour", "minute", "second", "nano"}},
 * {@code {"date", "time"}}), which the models follow. Registered in the mapper of the {@link SerdeJsonCodec} only.
 *
 * @since 2.4.0
 */
@Factory
@Internal
@Requires(property = SerdeJsonCodec.CODEC_PROPERTY, value = "true")
final class TemporalObjectDeserializers {

    private static final Set<String> DATE_KEYS = Set.of("year", "month", "day");
    private static final Set<String> TIME_KEYS = Set.of("hour", "minute", "second", "nano");
    private static final Set<String> DATE_TIME_KEYS = Set.of("date", "time");

    @Singleton
    Deserializer<LocalDate> localDateDeserializer() {
        return new TemporalDeserializer<>(LocalDate::parse, TemporalObjectDeserializers::localDate);
    }

    @Singleton
    Deserializer<LocalTime> localTimeDeserializer() {
        return new TemporalDeserializer<>(LocalTime::parse, TemporalObjectDeserializers::localTime);
    }

    @Singleton
    Deserializer<LocalDateTime> localDateTimeDeserializer() {
        return new TemporalDeserializer<>(LocalDateTime::parse, fields -> {
            checkKeys(fields, DATE_TIME_KEYS);
            return LocalDateTime.of(localDate(object(fields.get("date"))), localTime(object(fields.get("time"))));
        });
    }

    /**
     * Rejects a property the shape does not have, as the codec rejects the unknown properties of the other types.
     */
    private static void checkKeys(Map<?, ?> fields, Set<String> keys) {
        for (Object key : fields.keySet()) {
            if (!keys.contains(String.valueOf(key))) {
                throw new IllegalArgumentException("Unknown property '" + key + "', expected " + keys);
            }
        }
    }

    private static LocalDate localDate(Map<?, ?> fields) {
        checkKeys(fields, DATE_KEYS);
        return LocalDate.of(number(fields, "year"), number(fields, "month"), number(fields, "day"));
    }

    private static LocalTime localTime(Map<?, ?> fields) {
        checkKeys(fields, TIME_KEYS);
        return LocalTime.of(number(fields, "hour"), number(fields, "minute"), optionalNumber(fields, "second"), optionalNumber(fields, "nano"));
    }

    private static Map<?, ?> object(@Nullable Object value) {
        if (value instanceof Map<?, ?> map) {
            return map;
        }
        throw new IllegalArgumentException("Expected an object, got: " + value);
    }

    private static int number(Map<?, ?> fields, String name) {
        if (fields.get(name) instanceof Number number) {
            return number.intValue();
        }
        throw new IllegalArgumentException("Expected a number for '" + name + "', got: " + fields.get(name));
    }

    private static int optionalNumber(Map<?, ?> fields, String name) {
        return fields.get(name) == null ? 0 : number(fields, name);
    }

    /**
     * A deserializer reading a temporal from its ISO string or its object shape.
     *
     * @param <T> The temporal type
     */
    private static final class TemporalDeserializer<T> implements Deserializer<T> {

        private final Function<String, T> parser;
        private final Function<Map<?, ?>, T> fromFields;

        TemporalDeserializer(Function<String, T> parser, Function<Map<?, ?>, T> fromFields) {
            this.parser = parser;
            this.fromFields = fromFields;
        }

        @Override
        public T deserialize(Decoder decoder, DecoderContext context, Argument<? super T> type) throws IOException {
            Object value = decoder.decodeArbitrary();
            try {
                if (value instanceof String string) {
                    return parser.apply(string);
                }
                return fromFields.apply(object(value));
            } catch (RuntimeException e) {
                throw new SerdeException("Cannot read " + type.getTypeName() + " from " + value + ": " + e.getMessage(), e);
            }
        }
    }
}
