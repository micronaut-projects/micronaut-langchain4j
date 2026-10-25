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

import dev.langchain4j.model.chat.ChatModel;
import org.jspecify.annotations.NonNull;

/**
 * Identifies chat models that accept a JSON schema response format on each request although they do not declare
 * {@link dev.langchain4j.model.chat.Capability#RESPONSE_FORMAT_JSON_SCHEMA}.
 *
 * <p>LangChain4j only sends a JSON schema to models declaring the capability, and otherwise appends
 * format instructions to the user message. Google AI Gemini, for example, honors the response format of the
 * request but declares the capability only when a JSON response format is configured on the model. For the
 * models identified here, the AI services and agents returning a type with a generated schema use the
 * structured output of the model.</p>
 *
 * <p>Provider modules register a bean for their models; the
 * {@code langchain4j.structured-output.force-json-schema-response-format} property overrides them.</p>
 *
 * @since 2.4.0
 */
@FunctionalInterface
public interface JsonSchemaResponseFormatSupport {

    /**
     * Whether the model sends the JSON schema of the response format of a request.
     *
     * @param chatModel The chat model
     * @return Whether the model accepts a JSON schema response format on the request
     */
    boolean acceptsJsonSchemaResponseFormat(@NonNull ChatModel chatModel);
}
