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
package io.micronaut.langchain4j.jsonschema;

import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.io.Readable;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.jsonschema.utils.JsonSchemaClassPathResourceLoader;
import io.micronaut.jsonschema.utils.JsonSchemaConfiguration;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedJsonSchemaProviderTest {

    private final ApplicationContext context = ApplicationContext.run();
    private final Map<Class<?>, String> schemasByType = new LinkedHashMap<>();
    private final Map<String, Readable> allSchemas = new LinkedHashMap<>();
    private final AtomicInteger lookups = new AtomicInteger();
    private volatile Runnable onLookup = () -> { };
    private final GeneratedJsonSchemaProvider provider = new GeneratedJsonSchemaProvider(new JsonSchemaClassPathResourceLoader() {
        @Override
        public <T> Optional<String> jsonSchemaStringForClass(@NonNull Class<T> type) {
            return Optional.ofNullable(schemasByType.get(type));
        }

        @Override
        public @NonNull Map<String, Readable> jsonSchemas() {
            lookups.incrementAndGet();
            onLookup.run();
            return allSchemas;
        }
    }, new JsonSchemaConfiguration() {
        @Override
        public String getOutputLocation() {
            // no generated file is found by name: only the loader above supplies schemas
            return "missing";
        }
    }, context);

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void annotatedTypeWithoutSchema() {
        assertTrue(provider.findSchema(Annotated.class).isEmpty());
    }

    @Test
    void typeNotAnnotatedAtRuntimeIsMatchedByTitle() {
        schemasByType.put(NotAnnotated.class, """
            {"title": "NotAnnotated", "type": "object", "properties": {"name": {"type": "string"}}}""");
        schemasByType.put(Mismatch.class, """
            {"title": "Something", "type": "object"}""");

        assertTrue(((JsonObjectSchema) provider.findSchema(NotAnnotated.class).orElseThrow()).properties().containsKey("name"));
        assertTrue(provider.findSchema(Mismatch.class).isEmpty());
    }

    @Test
    void unsupportedSchema() {
        schemasByType.put(Annotated.class, """
            {"type": "object", "properties": {"x": {"type": "tuple"}}}""");

        assertTrue(provider.findSchema(Annotated.class).isEmpty());
    }

    @Test
    void referencesAreLookedUpInAllSchemas() {
        schemasByType.put(Annotated.class, """
            {"type": "object", "properties": {"address": {"$ref": "urn:example:address"}}}""");
        allSchemas.put("broken.schema.json", readable("broken", null));
        allSchemas.put("invalid.schema.json", readable("invalid", "{not json"));
        allSchemas.put("address.schema.json", readable("address", """
            {"$id": "urn:example:address", "type": "object", "properties": {"city": {"type": "string"}}}"""));

        JsonObjectSchema schema = (JsonObjectSchema) provider.findSchema(Annotated.class).orElseThrow();

        JsonObjectSchema address = (JsonObjectSchema) schema.properties().get("address");
        assertEquals(1, address.properties().size());
        // the result is cached
        assertEquals(schema, provider.findSchema(Annotated.class).orElseThrow());
    }

    @Test
    void concurrentLookupsLookUpAllSchemasOnce() throws InterruptedException {
        schemasByType.put(Annotated.class, """
            {"type": "object", "properties": {"address": {"$ref": "urn:example:address"}}}""");
        schemasByType.put(NotAnnotated.class, """
            {"title": "NotAnnotated", "type": "object", "properties": {"contact": {"$ref": "urn:example:contact"}}}""");
        allSchemas.put("address.schema.json", readable("address", """
            {"$id": "urn:example:address", "type": "object", "properties": {"city": {"type": "string"}}}"""));
        allSchemas.put("contact.schema.json", readable("contact", """
            {"$id": "urn:example:contact", "type": "object", "properties": {"email": {"type": "string"}}}"""));
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch released = new CountDownLatch(1);
        onLookup = () -> {
            started.countDown();
            try {
                released.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread first = Thread.ofPlatform().start(() -> provider.findSchema(Annotated.class));
        assertTrue(started.await(10, TimeUnit.SECONDS));
        Thread second = Thread.ofPlatform().start(() -> provider.findSchema(NotAnnotated.class));
        // the second lookup has to wait for the first one, not to repeat it
        Set<Thread.State> waiting = Set.of(Thread.State.WAITING, Thread.State.TIMED_WAITING, Thread.State.BLOCKED);
        for (long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
             !waiting.contains(second.getState()) && System.nanoTime() < deadline;) {
            Thread.onSpinWait();
        }
        released.countDown();
        first.join();
        second.join();

        assertEquals(1, lookups.get());
        assertTrue(((JsonObjectSchema) provider.findSchema(Annotated.class).orElseThrow()).properties().containsKey("address"));
        assertTrue(((JsonObjectSchema) provider.findSchema(NotAnnotated.class).orElseThrow()).properties().containsKey("contact"));
    }

    private static Readable readable(String name, String content) {
        return new Readable() {
            @Override
            public InputStream asInputStream() throws IOException {
                if (content == null) {
                    throw new IOException("unreadable");
                }
                return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public @NonNull String getName() {
                return name;
            }
        };
    }

    @JsonSchema
    static final class Annotated {
    }

    static final class NotAnnotated {
    }

    static final class Mismatch {
    }
}
