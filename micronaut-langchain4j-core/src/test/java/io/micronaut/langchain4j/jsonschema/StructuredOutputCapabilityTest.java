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

import dev.langchain4j.data.message.UserMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.jsonschema.StructuredOutputTypes.PlannedDay;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Models that do not declare the JSON schema response format capability.
 */
class StructuredOutputCapabilityTest {

    static final String SPEC_NAME = "StructuredOutputCapabilityTest";

    @Test
    void formatInstructionsByDefault() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("spec.name", SPEC_NAME))) {
            SchemaRecordingChatModel chatModel = context.getBean(SchemaRecordingChatModel.class);

            assertEquals("Monday", context.getBean(Planner.class).planDay("Monday").day());

            assertNull(chatModel.lastRequest().responseFormat());
            String userMessage = ((UserMessage) chatModel.lastRequest().messages().getLast()).singleText();
            assertTrue(userMessage.contains("You must answer strictly"));
        }
    }

    @Test
    void forcedJsonSchemaResponseFormat() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.structured-output.force-json-schema-response-format", true))) {
            SchemaRecordingChatModel chatModel = context.getBean(SchemaRecordingChatModel.class);

            assertEquals("Monday", context.getBean(Planner.class).planDay("Monday").day());

            assertEquals("The talks planned for a day.",
                chatModel.lastRequest().responseFormat().jsonSchema().rootElement().description());
            String userMessage = ((UserMessage) chatModel.lastRequest().messages().getLast()).singleText();
            assertFalse(userMessage.contains("You must answer strictly"));
        }
    }

    @Test
    void providerSupport() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "spec.provider-support", true))) {
            SchemaRecordingChatModel chatModel = context.getBean(SchemaRecordingChatModel.class);

            assertEquals("Monday", context.getBean(Planner.class).planDay("Monday").day());

            assertEquals("PlannedDay", chatModel.lastRequest().responseFormat().jsonSchema().name());
        }
    }

    @Test
    void disabled() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "spec.provider-support", true,
            "langchain4j.structured-output.enabled", false))) {
            SchemaRecordingChatModel chatModel = context.getBean(SchemaRecordingChatModel.class);

            assertFalse(context.containsBean(StructuredOutputSchemas.class));
            assertEquals("Monday", context.getBean(Planner.class).planDay("Monday").day());

            assertNull(chatModel.lastRequest().responseFormat());
        }
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Planner {
        PlannedDay planDay(String day);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestBeans {

        @Singleton
        SchemaRecordingChatModel chatModel() {
            return new SchemaRecordingChatModel(Set.of(), Map.of("PlannedDay", StructuredOutputAiServiceTest.answers().get("PlannedDay")));
        }

        @Singleton
        @Requires(property = "spec.provider-support", value = "true")
        JsonSchemaResponseFormatSupport recordingModelSupport() {
            return SchemaRecordingChatModel.class::isInstance;
        }
    }
}
