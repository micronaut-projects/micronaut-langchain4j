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

import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import io.micronaut.core.annotation.Internal;

import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * The JSON schemas of the structured outputs of one AI service or agent, keyed by the name LangChain4j gives the
 * schema of the response format (the simple name of the return type, or {@code List_of_<Type>} for a list).
 *
 * <p>Applied to a {@link ChatRequest}, it replaces the schema LangChain4j derived reflectively with the provided one.</p>
 *
 * @param schemas The schemas by name
 */
@Internal
public record ResponseFormatSchemas(Map<String, JsonSchema> schemas) implements UnaryOperator<ChatRequest> {

    @Override
    public ChatRequest apply(ChatRequest chatRequest) {
        ResponseFormat responseFormat = chatRequest.responseFormat();
        if (responseFormat == null || responseFormat.jsonSchema() == null) {
            return chatRequest;
        }
        JsonSchema schema = schemas.get(responseFormat.jsonSchema().name());
        if (schema == null) {
            return chatRequest;
        }
        return chatRequest.toBuilder()
            .responseFormat(ResponseFormat.builder().type(ResponseFormatType.JSON).jsonSchema(schema).build())
            .build();
    }
}
