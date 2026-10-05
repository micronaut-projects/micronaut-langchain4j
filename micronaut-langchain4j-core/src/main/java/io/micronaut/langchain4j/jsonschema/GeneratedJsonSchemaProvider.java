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

import dev.langchain4j.internal.Json;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.io.Readable;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.naming.NameUtils;
import io.micronaut.jsonschema.JsonSchema;
import io.micronaut.jsonschema.utils.JsonSchemaClassPathResourceLoader;
import io.micronaut.jsonschema.utils.JsonSchemaConfiguration;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Supplies the JSON schemas generated at compile time by Micronaut JSON Schema for the types annotated with
 * {@link JsonSchema}.
 *
 * <p>The schemas are loaded with {@link JsonSchemaClassPathResourceLoader}, so the type itself is never inspected
 * reflectively. References to the schemas of other types are resolved by their {@code $id}.</p>
 *
 * <p>A type is looked up when it is annotated with {@link JsonSchema}, or when a schema named after it is found
 * whose title is its name: the Java class generated for a Python class carries the annotation only when reflection
 * is allowed for it.</p>
 */
@Singleton
@Internal
@Requires(classes = JsonSchemaClassPathResourceLoader.class)
@Requires(beans = JsonSchemaClassPathResourceLoader.class)
final class GeneratedJsonSchemaProvider implements StructuredOutputSchemaProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratedJsonSchemaProvider.class);

    private final JsonSchemaClassPathResourceLoader loader;
    private final ResourceLoader resourceLoader;
    private final String schemasFolder;
    private final JsonSchemaConverter converter = new JsonSchemaConverter(this::findDocument);
    private final Map<Class<?>, Optional<JsonSchemaElement>> schemas = new ConcurrentHashMap<>();
    private final Map<URI, Map<String, Object>> documentsById = new ConcurrentHashMap<>();
    private volatile boolean allDocumentsLoaded;

    GeneratedJsonSchemaProvider(JsonSchemaClassPathResourceLoader loader,
                                JsonSchemaConfiguration configuration,
                                BeanContext beanContext) {
        this.loader = loader;
        this.resourceLoader = ClassPathResourceLoader.defaultLoader(beanContext.getClassLoader());
        this.schemasFolder = "META-INF/" + configuration.getOutputLocation() + "/";
    }

    @Override
    public @NonNull Optional<JsonSchemaElement> findSchema(@NonNull Class<?> type) {
        return schemas.computeIfAbsent(type, this::load);
    }

    private Optional<JsonSchemaElement> load(Class<?> type) {
        // the generated class of a Python class carries the annotation only when reflection is allowed for it
        boolean annotated = isAnnotated(type);
        Optional<String> source = loader.jsonSchemaStringForClass(type).or(() -> nestedTypeSchema(type));
        if (source.isEmpty()) {
            if (annotated) {
                LOG.warn("No JSON schema generated for {} although it is annotated with @JsonSchema", type.getName());
            }
            return Optional.empty();
        }
        try {
            Map<String, Object> document = parse(source.get());
            if (!annotated && !isSchemaOf(document, type)) {
                LOG.debug("The JSON schema {} is not the schema of {}", document.get("$id"), type.getName());
                return Optional.empty();
            }
            register(document);
            return Optional.of(converter.convert(document));
        } catch (RuntimeException e) {
            LOG.warn("The JSON schema generated for {} is not supported, LangChain4j derives the schema instead: {}", type.getName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * The schema of a nested type is named after the enclosing types too ({@code outer-inner.schema.json}),
     * which the loader does not look up unless the type is introspected.
     */
    private Optional<String> nestedTypeSchema(Class<?> type) {
        if (type.getEnclosingClass() == null) {
            return Optional.empty();
        }
        String packageName = type.getPackageName();
        String nestedName = type.getName().substring(packageName.isEmpty() ? 0 : packageName.length() + 1).replace("$", "");
        return resourceLoader.getResourceAsStream(schemasFolder + NameUtils.hyphenate(nestedName) + ".schema.json")
            .flatMap(schema -> {
                try (InputStream inputStream = schema) {
                    return Optional.of(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LOG.debug("Unable to read the JSON schema of {}", type.getName(), e);
                    return Optional.empty();
                }
            });
    }

    /**
     * Whether a schema found by the name of a type that is not known to be annotated was generated for it:
     * the title defaults to the (nested) name of the class.
     */
    private static boolean isSchemaOf(Map<String, Object> document, Class<?> type) {
        String packageName = type.getPackageName();
        String nestedName = type.getName().substring(packageName.isEmpty() ? 0 : packageName.length() + 1).replace('$', '.');
        Object title = document.get("title");
        return type.getSimpleName().equals(title) || nestedName.equals(title);
    }

    private static boolean isAnnotated(Class<?> type) {
        if (type.isAnnotationPresent(JsonSchema.class)) {
            return true;
        }
        // classes compiled from other languages may only carry the annotation in their introspection
        return BeanIntrospector.SHARED.findIntrospection(type)
            .map(introspection -> introspection.hasAnnotation(JsonSchema.class))
            .orElse(false);
    }

    private Optional<Map<String, Object>> findDocument(URI id) {
        Map<String, Object> document = documentsById.get(id);
        if (document == null) {
            // the generated schemas are named after the last segment of their $id
            String path = id.getPath();
            String fileName = path != null ? path.substring(path.lastIndexOf('/') + 1) : "";
            if (!fileName.isEmpty()) {
                resourceLoader.getResourceAsStream(schemasFolder + fileName).ifPresent(this::read);
                document = documentsById.get(id);
            }
        }
        if (document == null && !allDocumentsLoaded) {
            // a schema in a sub-folder or with another base URI: look all of them up, where the classpath allows it
            for (Readable readable : loader.jsonSchemas().values()) {
                try {
                    read(readable.asInputStream());
                } catch (IOException e) {
                    LOG.debug("Unable to read JSON schema {}", readable.getName(), e);
                }
            }
            allDocumentsLoaded = true;
            document = documentsById.get(id);
        }
        return Optional.ofNullable(document);
    }

    private void read(InputStream schema) {
        try (InputStream inputStream = schema) {
            register(parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)));
        } catch (IOException | RuntimeException e) {
            LOG.debug("Unable to read JSON schema", e);
        }
    }

    private void register(Map<String, Object> document) {
        if (document.get("$id") instanceof String id) {
            documentsById.putIfAbsent(URI.create(id), document);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(String json) {
        return Json.fromJson(json, Map.class);
    }
}
