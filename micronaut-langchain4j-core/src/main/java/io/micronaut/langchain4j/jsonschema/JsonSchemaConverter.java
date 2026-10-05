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

import dev.langchain4j.model.chat.request.json.JsonAnyOfSchema;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNullSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonReferenceSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * Converts JSON Schema documents (the draft 2020-12 subset produced by Micronaut JSON Schema) into the
 * LangChain4j {@link JsonSchemaElement} model.
 *
 * <p>References ({@code $ref}) are resolved within the document ({@code #/$defs/...}, {@code #/definitions/...},
 * {@code #}) and across documents by their {@code $id}. Acyclic references are inlined, which every provider
 * supports. A reference cycle is expressed with {@link JsonReferenceSchema} and definitions on the root
 * object, because it cannot be inlined.</p>
 *
 * <p>Validation keywords have no counterpart in the LangChain4j model, so they are appended to the description,
 * where the model still sees them.</p>
 */
@Internal
final class JsonSchemaConverter {

    private static final String REF = "$ref";
    private static final String ID = "$id";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String PROPERTIES = "properties";
    private static final String ITEMS = "items";
    private static final String NULL = "null";
    private static final List<String> CONSTRAINT_KEYWORDS = List.of(
        "format", "pattern", "minLength", "maxLength",
        "minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum", "multipleOf",
        "minItems", "maxItems", "uniqueItems", "minProperties", "maxProperties"
    );

    private final Function<URI, Optional<Map<String, Object>>> documentResolver;

    /**
     * @param documentResolver Resolves another schema document by its {@code $id} (without fragment)
     */
    JsonSchemaConverter(Function<URI, Optional<Map<String, Object>>> documentResolver) {
        this.documentResolver = documentResolver;
    }

    /**
     * Converts a schema document.
     *
     * @param document The parsed JSON Schema document
     * @return The root element, a {@link JsonObjectSchema} carrying definitions if the schema is recursive
     */
    JsonSchemaElement convert(Map<String, Object> document) {
        return new Conversion().convertRoot(new Document(document));
    }

    private static List<String> types(Map<String, Object> schema) {
        Object type = schema.get(TYPE);
        List<String> types = new ArrayList<>();
        if (type instanceof String single) {
            types.add(single);
        } else if (type instanceof List<?> multiple) {
            multiple.forEach(t -> types.add(t.toString()));
        } else if (schema.containsKey(ITEMS)) {
            types.add("array");
        } else {
            types.add("object");
        }
        if (types.size() > 1) {
            // nullable types are expressed as ["string", "null"]: the model only needs the value type
            types.remove(NULL);
        }
        return types;
    }

    private static List<String> enumValues(List<?> values) {
        return values.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    private static @Nullable String description(Map<String, Object> schema) {
        String description = schema.get(DESCRIPTION) instanceof String d && !d.isBlank() ? d.strip() : null;
        StringJoiner constraints = new StringJoiner(", ");
        for (String keyword : CONSTRAINT_KEYWORDS) {
            Object value = schema.get(keyword);
            if (value != null && !(value instanceof Map) && !(value instanceof List)) {
                constraints.add(keyword + ": " + value);
            }
        }
        if (constraints.length() == 0) {
            return description;
        }
        return (description != null ? description + " " : "") + "(" + constraints + ")";
    }

    private static JsonSchemaElement withDescription(JsonSchemaElement element, @Nullable String description) {
        if (description == null || description.equals(element.description())) {
            return element;
        }
        return switch (element) {
            case JsonObjectSchema s -> s.toBuilder().description(description).build();
            case JsonArraySchema s -> JsonArraySchema.builder().description(description).items(s.items()).build();
            case JsonStringSchema _ -> JsonStringSchema.builder().description(description).build();
            case JsonIntegerSchema _ -> JsonIntegerSchema.builder().description(description).build();
            case JsonNumberSchema _ -> JsonNumberSchema.builder().description(description).build();
            case JsonBooleanSchema _ -> JsonBooleanSchema.builder().description(description).build();
            case JsonEnumSchema s -> JsonEnumSchema.builder().description(description).enumValues(s.enumValues()).build();
            case JsonAnyOfSchema s -> JsonAnyOfSchema.builder().description(description).anyOf(s.anyOf()).build();
            default -> element;
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asSchema(Map<?, ?> map) {
        return (Map<String, Object>) map;
    }

    private record Document(Map<String, Object> root, @Nullable URI id) {
        Document(Map<String, Object> root) {
            this(root, idOf(root));
        }

        private static @Nullable URI idOf(Map<String, Object> schema) {
            return schema.get(ID) instanceof String id ? URI.create(id) : null;
        }

        String key(String pointer) {
            return (id != null ? id.toString() : "") + "#" + pointer;
        }
    }

    private record Target(Document document, Map<String, Object> schema, String pointer) {
    }

    /**
     * The state of one conversion: the references being expanded and the definitions of recursive ones.
     */
    private final class Conversion {
        private final Map<String, JsonSchemaElement> definitions = new LinkedHashMap<>();
        private final Map<String, String> definitionNames = new LinkedHashMap<>();
        private final Set<String> expanding = new HashSet<>();
        private final Set<String> recursive = new HashSet<>();

        JsonSchemaElement convertRoot(Document document) {
            JsonSchemaElement root = expand(new Target(document, document.root(), ""));
            if (definitions.isEmpty()) {
                return root;
            }
            JsonSchemaElement rootElement = root instanceof JsonReferenceSchema reference
                ? definitions.get(reference.reference())
                : root;
            if (!(rootElement instanceof JsonObjectSchema objectSchema)) {
                throw new IllegalArgumentException("A recursive JSON schema must have an object at its root");
            }
            return objectSchema.toBuilder().definitions(definitions).build();
        }

        private JsonSchemaElement expand(Target target) {
            String key = target.document().key(target.pointer());
            String name = definitionName(key, target);
            if (!expanding.add(key)) {
                recursive.add(key);
                return JsonReferenceSchema.builder().reference(name).build();
            }
            JsonSchemaElement element;
            try {
                element = element(target.schema(), target.document());
            } finally {
                expanding.remove(key);
            }
            if (recursive.contains(key)) {
                definitions.put(name, element);
                return JsonReferenceSchema.builder().reference(name).build();
            }
            return element;
        }

        private JsonSchemaElement element(Map<String, Object> schema, Document document) {
            String description = description(schema);
            if (schema.get(REF) instanceof String ref) {
                return withDescription(expand(resolve(ref, document)), description);
            }
            if (schema.get("enum") instanceof List<?> values) {
                return JsonEnumSchema.builder().description(description).enumValues(enumValues(values)).build();
            }
            if (schema.containsKey("const")) {
                return JsonEnumSchema.builder().description(description).enumValues(enumValues(Collections.singletonList(schema.get("const")))).build();
            }
            if (schema.get("anyOf") instanceof List<?> variants) {
                return anyOf(variants, description, document);
            }
            if (schema.get("oneOf") instanceof List<?> variants) {
                return anyOf(variants, description, document);
            }
            if (schema.get("allOf") instanceof List<?> parts) {
                return allOf(parts, schema, description, document);
            }
            List<String> types = types(schema);
            if (types.size() > 1) {
                List<JsonSchemaElement> variants = types.stream().map(type -> typed(type, schema, null, document)).toList();
                return JsonAnyOfSchema.builder().description(description).anyOf(variants).build();
            }
            return typed(types.getFirst(), schema, description, document);
        }

        private JsonSchemaElement typed(String type, Map<String, Object> schema, @Nullable String description, Document document) {
            return switch (type) {
                case "object" -> object(schema, description, document);
                case "array" -> JsonArraySchema.builder()
                    .description(description)
                    .items(schema.get(ITEMS) instanceof Map<?, ?> items ? element(asSchema(items), document) : null)
                    .build();
                case "string" -> JsonStringSchema.builder().description(description).build();
                case "integer" -> JsonIntegerSchema.builder().description(description).build();
                case "number" -> JsonNumberSchema.builder().description(description).build();
                case "boolean" -> JsonBooleanSchema.builder().description(description).build();
                case NULL -> new JsonNullSchema();
                default -> throw new IllegalArgumentException("Unsupported JSON schema type '" + type + "'");
            };
        }

        private JsonObjectSchema object(Map<String, Object> schema, @Nullable String description, Document document) {
            JsonObjectSchema.Builder builder = JsonObjectSchema.builder().description(description);
            if (schema.get(PROPERTIES) instanceof Map<?, ?> properties) {
                for (Map.Entry<?, ?> property : properties.entrySet()) {
                    if (property.getValue() instanceof Map<?, ?> propertySchema) {
                        builder.addProperty(property.getKey().toString(), element(asSchema(propertySchema), document));
                    }
                }
            }
            if (schema.get("required") instanceof List<?> required && !required.isEmpty()) {
                builder.required(required.stream().map(Object::toString).toList());
            }
            if (schema.get("additionalProperties") instanceof Boolean additionalProperties) {
                builder.additionalProperties(additionalProperties);
            }
            return builder.build();
        }

        private JsonSchemaElement anyOf(List<?> variants, @Nullable String description, Document document) {
            List<JsonSchemaElement> elements = new ArrayList<>(variants.size());
            boolean nullable = false;
            for (Object variant : variants) {
                if (variant instanceof Map<?, ?> variantSchema) {
                    JsonSchemaElement element = element(asSchema(variantSchema), document);
                    if (element instanceof JsonNullSchema) {
                        nullable = true;
                    } else {
                        elements.add(element);
                    }
                }
            }
            if (elements.size() == 1) {
                // a nullable value: the model only needs the value type, leaving out a property expresses null
                return withDescription(elements.getFirst(), description);
            }
            if (elements.isEmpty() && nullable) {
                return new JsonNullSchema();
            }
            return JsonAnyOfSchema.builder().description(description).anyOf(elements).build();
        }

        private JsonSchemaElement allOf(List<?> parts, Map<String, Object> schema, @Nullable String description, Document document) {
            List<JsonSchemaElement> elements = new ArrayList<>(parts.stream()
                .filter(Map.class::isInstance)
                .map(part -> element(asSchema((Map<?, ?>) part), document))
                .toList());
            if (schema.containsKey(PROPERTIES)) {
                // properties declared next to allOf (for example by a subclass) belong to the intersection
                elements.add(object(schema, null, document));
            }
            if (elements.size() == 1) {
                return withDescription(elements.getFirst(), description);
            }
            if (elements.stream().allMatch(JsonObjectSchema.class::isInstance)) {
                return merge(elements.stream().map(JsonObjectSchema.class::cast).toList(), description);
            }
            throw new IllegalArgumentException("Unsupported JSON schema: allOf is only supported for objects");
        }

        private JsonObjectSchema merge(List<JsonObjectSchema> objectSchemas, @Nullable String description) {
            JsonObjectSchema.Builder merged = JsonObjectSchema.builder().description(description);
            Map<String, JsonSchemaElement> properties = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();
            for (JsonObjectSchema objectSchema : objectSchemas) {
                objectSchema.properties().forEach((name, property) ->
                    properties.merge(name, property, (declared, redeclared) -> intersect(name, declared, redeclared)));
                objectSchema.required().stream().filter(r -> !required.contains(r)).forEach(required::add);
                if (objectSchema.additionalProperties() != null) {
                    merged.additionalProperties(objectSchema.additionalProperties());
                }
            }
            merged.addProperties(properties);
            if (!required.isEmpty()) {
                merged.required(required);
            }
            return merged.build();
        }

        /**
         * A property declared by several parts of an allOf (for example redeclared by a subclass) has to satisfy
         * all of them: objects are merged, and the last description of an otherwise identical schema is kept.
         * Anything else has no counterpart in the LangChain4j model.
         */
        private JsonSchemaElement intersect(String name, JsonSchemaElement declared, JsonSchemaElement redeclared) {
            String description = redeclared.description() != null ? redeclared.description() : declared.description();
            if (declared instanceof JsonObjectSchema first && redeclared instanceof JsonObjectSchema second) {
                return merge(List.of(first, second), description);
            }
            JsonSchemaElement element = withDescription(redeclared, description);
            if (element.equals(withDescription(declared, description))) {
                return element;
            }
            throw new IllegalArgumentException("Unsupported JSON schema: allOf declares the property '" + name + "' with different schemas");
        }

        private Target resolve(String ref, Document document) {
            int hash = ref.indexOf('#');
            String location = hash >= 0 ? ref.substring(0, hash) : ref;
            String fragment = hash >= 0 ? ref.substring(hash + 1) : "";
            // "#" and "#/" both point to the root
            String pointer = "/".equals(fragment) ? "" : fragment;
            Document target = document;
            if (!location.isEmpty()) {
                URI uri = document.id() != null ? document.id().resolve(location) : URI.create(location);
                if (!uri.equals(document.id())) {
                    target = documentResolver.apply(uri)
                        .map(Document::new)
                        .orElseThrow(() -> new IllegalArgumentException("Unresolved JSON schema reference '" + ref + "'"));
                }
            }
            return new Target(target, navigate(target.root(), pointer, ref), pointer);
        }

        private Map<String, Object> navigate(Map<String, Object> root, String pointer, String ref) {
            Object current = root;
            if (!pointer.isEmpty()) {
                for (String token : pointer.substring(pointer.startsWith("/") ? 1 : 0).split("/")) {
                    String segment = token.replace("~1", "/").replace("~0", "~");
                    current = current instanceof Map<?, ?> map ? map.get(segment) : null;
                }
            }
            if (current instanceof Map<?, ?> schema) {
                return asSchema(schema);
            }
            throw new IllegalArgumentException("Unresolved JSON schema reference '" + ref + "'");
        }

        private String definitionName(String key, Target target) {
            return definitionNames.computeIfAbsent(key, k -> {
                String name;
                String pointer = target.pointer();
                if (!pointer.isEmpty()) {
                    name = pointer.substring(pointer.lastIndexOf('/') + 1);
                } else if (target.document().root().get("title") instanceof String title) {
                    name = title;
                } else if (target.document().id() != null) {
                    URI id = target.document().id();
                    // a URN has no path
                    String path = id.getPath() != null ? id.getPath() : id.getSchemeSpecificPart();
                    name = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf(':')) + 1).replace(".schema.json", "").replace(".json", "");
                } else {
                    name = "root";
                }
                name = name.replaceAll("[^A-Za-z0-9_.-]", "_");
                String unique = name;
                for (int i = 2; definitionNames.containsValue(unique); i++) {
                    unique = name + i;
                }
                return unique;
            });
        }
    }
}
