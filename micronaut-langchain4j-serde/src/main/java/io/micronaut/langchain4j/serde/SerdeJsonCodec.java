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

import dev.langchain4j.exception.JsonReadException;
import dev.langchain4j.exception.JsonWriteException;
import dev.langchain4j.internal.Json;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.config.DeserializationConfiguration;
import io.micronaut.serde.config.SerializationConfiguration;
import io.micronaut.serde.support.DefaultSerdeIntrospections;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Supplier;

/**
 * LangChain4j's general purpose JSON codec ({@code dev.langchain4j.internal.Json}) backed by Micronaut Serialization:
 * the structured outputs of the AI services, the arguments and results of the tools and the arguments of the agents
 * are mapped with their compile-time {@code @Serdeable} introspections, without reflection.
 *
 * <p>A type without a {@code @Serdeable} introspection is mapped with the introspection the
 * {@link ReflectiveIntrospectionResolver} supplies: its {@code @Introspected} introspection, or one built
 * reflectively by micronaut-reflection from its fields and accessors.</p>
 *
 * <p>The codec is configured like LangChain4j's default Jackson codec: unknown properties are rejected (LangChain4j
 * relies on it to discard the JSON candidates it extracts from the answers of the models), enums are read case
 * insensitively, nulls and empty values are written, nulls are accepted for primitives, and dates are also read in
 * the object shape of the JSON schema LangChain4j derives for them ({@link TemporalObjectDeserializers}).</p>
 *
 * @since 2.4.0
 */
@Internal
final class SerdeJsonCodec implements Json.JsonCodec {

    /**
     * The property enabling the beans of this module in the mapper of the codec, and only there.
     */
    static final String CODEC_PROPERTY = "micronaut.langchain4j.serde.codec";

    /**
     * The configuration of the Micronaut Serialization mapper, mirroring LangChain4j's Jackson codec.
     */
    static final Map<String, Object> CONFIGURATION = Map.of(
        CODEC_PROPERTY, true,
        DeserializationConfiguration.PREFIX + ".ignore-unknown", false,
        DeserializationConfiguration.PREFIX + ".accept-case-insensitive-enums", true,
        DeserializationConfiguration.PREFIX + ".fail-on-null-for-primitives", false,
        SerializationConfiguration.PREFIX + ".inclusion", "ALWAYS"
    );

    // the mapper is created on first use: LangChain4j creates the codec when it loads its Json class
    private final Supplier<Mappers> mappers = SupplierUtil.memoized(SerdeJsonCodec::createMappers);

    @Override
    public String toJson(Object o) {
        try {
            return objectMapper().writeValueAsString(o);
        } catch (Exception e) {
            throw new JsonWriteException("Cannot write " + (o == null ? "null" : o.getClass().getName()) + " as JSON: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> @Nullable T fromJson(String json, Class<T> type) {
        return fromJson(json, (Type) type);
    }

    @Override
    public <T> @Nullable T fromJson(String json, Type type) {
        try {
            @SuppressWarnings("unchecked")
            Argument<T> argument = (Argument<T>) Argument.of(type);
            return objectMapper().readValue(json, argument);
        } catch (Exception e) {
            throw new JsonReadException("Cannot read " + type.getTypeName() + " from JSON: " + e.getMessage(), e);
        }
    }

    private ObjectMapper objectMapper() {
        return mappers.get().mapper();
    }

    /**
     * A mapper of its own: LangChain4j creates the codec once per class loader, independently of the application
     * contexts, and the configuration must not change the JSON of the application. The mapper, and the bean context
     * behind it, live as long as the codec, that is as long as LangChain4j's classes.
     */
    private static Mappers createMappers() {
        ObjectMapper.CloseableObjectMapper owner = ObjectMapper.create(CONFIGURATION, SerdeJsonCodec.class.getPackageName());
        return new Mappers(owner, owner.cloneWithConfiguration(null, null, null,
            new DefaultSerdeIntrospections().withRuntimeIntrospectionResolver(new ReflectiveIntrospectionResolver())));
    }

    /**
     * The mapper of the codec, and the mapper owning the bean context it was cloned from.
     *
     * @param owner  The mapper owning the bean context
     * @param mapper The mapper of the codec
     */
    private record Mappers(ObjectMapper.CloseableObjectMapper owner, ObjectMapper mapper) {
    }
}
