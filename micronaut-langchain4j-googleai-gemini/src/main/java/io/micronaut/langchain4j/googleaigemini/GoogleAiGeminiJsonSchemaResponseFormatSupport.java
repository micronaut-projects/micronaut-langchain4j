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
package io.micronaut.langchain4j.googleaigemini;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.micronaut.core.annotation.Internal;
import io.micronaut.langchain4j.jsonschema.JsonSchemaResponseFormatSupport;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;

/**
 * Google AI Gemini sends the JSON schema of the request as the response schema, but declares
 * {@link dev.langchain4j.model.chat.Capability#RESPONSE_FORMAT_JSON_SCHEMA} only when a JSON response format is
 * configured on the model.
 *
 * @since 2.4.0
 */
@Singleton
@Internal
final class GoogleAiGeminiJsonSchemaResponseFormatSupport implements JsonSchemaResponseFormatSupport {

    @Override
    public boolean acceptsJsonSchemaResponseFormat(@NonNull ChatModel chatModel) {
        return chatModel instanceof GoogleAiGeminiChatModel;
    }
}
