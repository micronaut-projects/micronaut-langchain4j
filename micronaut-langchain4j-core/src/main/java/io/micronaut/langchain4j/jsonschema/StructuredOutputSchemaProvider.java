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

import dev.langchain4j.model.chat.request.json.JsonSchemaElement;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Supplies the JSON schema sent to the chat model when an {@link io.micronaut.langchain4j.annotation.AiService}
 * or agent method returns the given type, instead of the schema LangChain4j derives reflectively.
 *
 * <p>When <code>micronaut-json-schema-utils</code> is on the classpath, a provider returns the schemas Micronaut
 * JSON Schema generated at compile time for the types annotated with <code>io.micronaut.jsonschema.JsonSchema</code>.
 * Register a bean of this type to supply schemas from another source; providers are consulted in order.</p>
 *
 * @since 2.4.0
 */
@FunctionalInterface
public interface StructuredOutputSchemaProvider {

    /**
     * Finds the schema of a structured output type.
     *
     * @param type The type returned by the service method (the element type for a {@code List} or {@code Set})
     * @return The schema of the type, or empty to let LangChain4j derive it
     */
    @NonNull
    Optional<JsonSchemaElement> findSchema(@NonNull Class<?> type);
}
