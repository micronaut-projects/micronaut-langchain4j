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

import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.langchain4j.jsonschema.JsonSchemaResponseFormatSupport;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleAiGeminiJsonSchemaResponseFormatSupportTest {

    @Test
    void geminiAcceptsTheJsonSchemaResponseFormat() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.google-ai-gemini.api-key", "test"))) {
            ChatModel chatModel = context.getBean(ChatModel.class);

            // without a configured JSON response format, Gemini does not declare the capability
            assertFalse(chatModel.supportedCapabilities().contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA));
            assertTrue(context.getBeansOfType(JsonSchemaResponseFormatSupport.class).stream()
                .anyMatch(support -> support.acceptsJsonSchemaResponseFormat(chatModel)));
        }
    }
}
