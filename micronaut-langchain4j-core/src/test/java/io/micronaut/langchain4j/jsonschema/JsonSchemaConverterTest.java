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
import dev.langchain4j.model.chat.request.json.JsonAnyOfSchema;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonBooleanSchema;
import dev.langchain4j.model.chat.request.json.JsonEnumSchema;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonNumberSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonReferenceSchema;
import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonSchemaConverterTest {

    private static final String ADDRESS = """
        {"$id": "https://example.com/schemas/address.schema.json", "title": "Address", "type": "object",
         "properties": {"city": {"type": "string"}}, "required": ["city"]}""";

    private final JsonSchemaConverter converter = new JsonSchemaConverter(id ->
        id.equals(URI.create("https://example.com/schemas/address.schema.json")) ? Optional.of(parse(ADDRESS)) : Optional.empty());

    @Test
    void scalarTypesAndConstraints() {
        JsonObjectSchema schema = convert("""
            {"type": "object", "description": "A person", "additionalProperties": false,
             "properties": {
               "name": {"type": "string", "description": "The name", "minLength": 1, "maxLength": 20},
               "age": {"type": "integer", "minimum": 0},
               "score": {"type": "number"},
               "active": {"type": "boolean"},
               "nickname": {"type": ["string", "null"], "description": "Optional nickname"},
               "kind": {"const": "person"},
               "level": {"enum": ["LOW", "HIGH", null]}
             },
             "required": ["name"]}""");

        assertEquals("A person", schema.description());
        assertEquals(false, schema.additionalProperties());
        assertEquals(List.of("name"), schema.required());
        Map<String, JsonSchemaElement> properties = schema.properties();
        assertEquals("The name (minLength: 1, maxLength: 20)", properties.get("name").description());
        assertInstanceOf(JsonIntegerSchema.class, properties.get("age"));
        assertEquals("(minimum: 0)", properties.get("age").description());
        assertInstanceOf(JsonNumberSchema.class, properties.get("score"));
        assertInstanceOf(JsonBooleanSchema.class, properties.get("active"));
        assertInstanceOf(JsonStringSchema.class, properties.get("nickname"));
        assertEquals("Optional nickname", properties.get("nickname").description());
        assertEquals(List.of("person"), ((JsonEnumSchema) properties.get("kind")).enumValues());
        assertEquals(List.of("LOW", "HIGH"), ((JsonEnumSchema) properties.get("level")).enumValues());
    }

    @Test
    void anyOfAndOneOf() {
        JsonObjectSchema schema = convert("""
            {"type": "object", "properties": {
               "value": {"anyOf": [{"type": "string"}, {"type": "integer"}]},
               "choice": {"oneOf": [{"type": "boolean"}, {"type": "number"}], "description": "A choice"},
               "nullable": {"anyOf": [{"type": "integer"}, {"type": "null"}], "description": "Maybe"},
               "multi": {"type": ["string", "integer"]}
            }}""");

        JsonAnyOfSchema value = (JsonAnyOfSchema) schema.properties().get("value");
        assertInstanceOf(JsonStringSchema.class, value.anyOf().get(0));
        assertInstanceOf(JsonIntegerSchema.class, value.anyOf().get(1));
        assertEquals("A choice", schema.properties().get("choice").description());
        assertEquals(2, ((JsonAnyOfSchema) schema.properties().get("choice")).anyOf().size());
        assertInstanceOf(JsonIntegerSchema.class, schema.properties().get("nullable"));
        assertEquals("Maybe", schema.properties().get("nullable").description());
        assertEquals(2, ((JsonAnyOfSchema) schema.properties().get("multi")).anyOf().size());
    }

    @Test
    void localAndExternalReferencesAreInlined() {
        JsonObjectSchema schema = convert("""
            {"$id": "https://example.com/schemas/person.schema.json", "type": "object",
             "properties": {
               "home": {"$ref": "address.schema.json", "description": "Where they live"},
               "work": {"$ref": "https://example.com/schemas/address.schema.json"},
               "pet": {"$ref": "#/$defs/Pet"},
               "legacy": {"$ref": "#/definitions/Legacy"},
               "tags": {"type": "array", "items": {"type": "string"}}
             },
             "$defs": {"Pet": {"type": "object", "properties": {"name": {"type": "string"}}}},
             "definitions": {"Legacy": {"type": "string"}}}""");

        JsonObjectSchema home = (JsonObjectSchema) schema.properties().get("home");
        assertEquals("Where they live", home.description());
        assertEquals(List.of("city"), home.required());
        assertEquals(home.properties(), ((JsonObjectSchema) schema.properties().get("work")).properties());
        assertTrue(((JsonObjectSchema) schema.properties().get("pet")).properties().containsKey("name"));
        assertInstanceOf(JsonStringSchema.class, schema.properties().get("legacy"));
        assertInstanceOf(JsonStringSchema.class, ((JsonArraySchema) schema.properties().get("tags")).items());
        assertTrue(schema.definitions().isEmpty());
    }

    @Test
    void recursiveReferencesUseDefinitions() {
        JsonObjectSchema schema = convert("""
            {"type": "object", "properties": {"root": {"$ref": "#/$defs/Node"}},
             "$defs": {"Node": {"type": "object", "properties": {
                "children": {"type": "array", "items": {"$ref": "#/$defs/Node"}}}}}}""");

        JsonReferenceSchema root = (JsonReferenceSchema) schema.properties().get("root");
        assertEquals("Node", root.reference());
        JsonObjectSchema node = (JsonObjectSchema) schema.definitions().get("Node");
        assertEquals(root, ((JsonArraySchema) node.properties().get("children")).items());
    }

    @Test
    void allOfMergesObjects() {
        JsonObjectSchema schema = convert("""
            {"allOf": [
               {"type": "object", "properties": {"a": {"type": "string"}}, "required": ["a"]},
               {"type": "object", "properties": {"b": {"type": "integer"}}, "required": ["b"]}
             ], "description": "Both"}""");

        assertEquals("Both", schema.description());
        assertEquals(List.of("a", "b"), schema.required());
        assertEquals(2, schema.properties().size());
    }

    @Test
    void unresolvedReference() {
        assertThrows(IllegalArgumentException.class, () -> convert("""
            {"type": "object", "properties": {"x": {"$ref": "https://example.com/missing.schema.json"}}}"""));
    }

    private JsonObjectSchema convert(String json) {
        return (JsonObjectSchema) converter.convert(parse(json));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(String json) {
        return Json.fromJson(json, Map.class);
    }
}
