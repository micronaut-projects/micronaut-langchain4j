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

import dev.langchain4j.exception.JsonReadException;
import dev.langchain4j.exception.JsonWriteException;
import dev.langchain4j.internal.Json;
import dev.langchain4j.internal.MicronautJacksonJsonCodecs;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.config.DeserializationConfiguration;
import io.micronaut.serde.config.SerializationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LangChain4j's general purpose JSON codec ({@code dev.langchain4j.internal.Json}) backed by Micronaut Serialization:
 * the structured outputs of the AI services, the arguments and results of the tools and the arguments of the agents
 * are mapped with their compile-time {@code @Serdeable} introspections, without reflection.
 *
 * <p>The codec is configured like LangChain4j's default Jackson codec: unknown properties are rejected (LangChain4j
 * relies on it to discard the JSON candidates it extracts from the answers of the models), enums are read case
 * insensitively, nulls and empty values are written and nulls are accepted for primitives.</p>
 *
 * <p>A type Micronaut Serialization cannot map, because it has no introspection, is mapped by LangChain4j's
 * Jackson codec, and remembered so that the following calls go to Jackson directly. A value that Micronaut
 * Serialization rejects for another reason, for example a date written by the model in the object shape LangChain4j's
 * schema describes, is also given to Jackson before the error is reported, so that the codec accepts what
 * LangChain4j's codec accepts.</p>
 *
 * @since 2.4.0
 */
@Internal
final class SerdeJsonCodec implements Json.JsonCodec {

    /**
     * The configuration of the Micronaut Serialization mapper, mirroring LangChain4j's Jackson codec.
     */
    static final Map<String, Object> CONFIGURATION = Map.of(
        DeserializationConfiguration.PREFIX + ".ignore-unknown", false,
        DeserializationConfiguration.PREFIX + ".accept-case-insensitive-enums", true,
        DeserializationConfiguration.PREFIX + ".fail-on-null-for-primitives", false,
        SerializationConfiguration.PREFIX + ".inclusion", "ALWAYS"
    );

    private static final Logger LOG = LoggerFactory.getLogger(SerdeJsonCodec.class);

    private final Set<Type> jacksonTypes = ConcurrentHashMap.newKeySet();
    private volatile ObjectMapper objectMapper;
    private volatile Json.JsonCodec jackson;

    @Override
    public String toJson(Object o) {
        if (o == null) {
            return "null";
        }
        Class<?> type = o.getClass();
        if (jacksonTypes.contains(type)) {
            return jackson().toJson(o);
        }
        try {
            return objectMapper().writeValueAsString(o);
        } catch (Exception e) {
            try {
                String json = jackson().toJson(o);
                if (isMissingIntrospection(e) && jacksonTypes.add(type) && LOG.isDebugEnabled()) {
                    LOG.debug("Mapping {} with Jackson: it has no Micronaut Serialization introspection ({})", type.getTypeName(), e.getMessage());
                }
                return json;
            } catch (RuntimeException jacksonError) {
                JsonWriteException error = new JsonWriteException("Cannot write " + type.getName() + " as JSON: " + e.getMessage(), e);
                error.addSuppressed(jacksonError);
                throw error;
            }
        }
    }

    @Override
    public <T> T fromJson(String json, Class<T> type) {
        return fromJson(json, (Type) type);
    }

    @Override
    public <T> T fromJson(String json, Type type) {
        if (jacksonTypes.contains(type)) {
            return jackson().fromJson(json, type);
        }
        try {
            @SuppressWarnings("unchecked")
            Argument<T> argument = (Argument<T>) Argument.of(type);
            return objectMapper().readValue(json, argument);
        } catch (Exception e) {
            try {
                T value = jackson().fromJson(json, type);
                if (isMissingIntrospection(e) && jacksonTypes.add(type) && LOG.isDebugEnabled()) {
                    LOG.debug("Mapping {} with Jackson: it has no Micronaut Serialization introspection ({})", type.getTypeName(), e.getMessage());
                }
                return value;
            } catch (RuntimeException jacksonError) {
                JsonReadException error = new JsonReadException("Cannot read " + type.getTypeName() + " from JSON: " + e.getMessage(), e);
                error.addSuppressed(jacksonError);
                throw error;
            }
        }
    }

    /**
     * @param type A type
     * @return Whether the type is mapped by Jackson, because Micronaut Serialization has no introspection for it
     */
    boolean isMappedByJackson(@NonNull Type type) {
        return jacksonTypes.contains(type);
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = objectMapper;
        if (mapper == null) {
            synchronized (this) {
                mapper = objectMapper;
                if (mapper == null) {
                    // a mapper of its own: LangChain4j creates the codec once per class loader, independently of the
                    // application contexts, and the configuration must not change the JSON of the application
                    mapper = ObjectMapper.create(CONFIGURATION);
                    objectMapper = mapper;
                }
            }
        }
        return mapper;
    }

    private Json.JsonCodec jackson() {
        Json.JsonCodec codec = jackson;
        if (codec == null) {
            synchronized (this) {
                codec = jackson;
                if (codec == null) {
                    codec = MicronautJacksonJsonCodecs.jackson();
                    jackson = codec;
                }
            }
        }
        return codec;
    }

    private static boolean isMissingIntrospection(Throwable error) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause instanceof IntrospectionException) {
                return true;
            }
        }
        return false;
    }
}
