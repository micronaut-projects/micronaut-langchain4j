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
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonReferenceSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.Result;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.langchain4j.jsonschema.StructuredOutputTypes.Category;
import io.micronaut.langchain4j.jsonschema.StructuredOutputTypes.Level;
import io.micronaut.langchain4j.jsonschema.StructuredOutputTypes.PlannedDay;
import io.micronaut.langchain4j.jsonschema.StructuredOutputTypes.PlannedTalk;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredOutputSchemasTest {

    private final ApplicationContext context = ApplicationContext.run();
    private final StructuredOutputSchemas structuredOutputSchemas = context.getBean(StructuredOutputSchemas.class);

    @AfterEach
    void close() {
        context.close();
    }

    @Test
    void unwrapsResultsAndAsyncTypes() {
        Map<String, JsonSchema> schemas = schemasFor(
            Argument.of(Result.class, PlannedDay.class),
            Argument.of(CompletableFuture.class, Category.class),
            Argument.of(CompletionStage.class, Argument.listOf(PlannedTalk.class)),
            Argument.setOf(PlannedTalk.class));

        assertEquals(Set.of("PlannedDay", "Category", "List_of_PlannedTalk", "Set_of_PlannedTalk"), schemas.keySet());
        assertEquals("The talks planned for a day.", schemas.get("PlannedDay").rootElement().description());
    }

    @Test
    void ignoresTypesWithoutGeneratedSchema() {
        assertTrue(structuredOutputSchemas.schemasFor(List.of(
            Argument.STRING, Argument.INT, Argument.of(Level.class), Argument.of(Runnable.class),
            Argument.of(String[].class), Argument.of(Result.class), Argument.of(List.class),
            Argument.of(StructuredOutputTypes.Plain.class))).isEmpty());
    }

    @Test
    void recursiveListElementsKeepTheirDefinitionsAtTheRoot() {
        JsonObjectSchema root = (JsonObjectSchema) schemasFor(Argument.listOf(Category.class)).get("List_of_Category").rootElement();

        JsonObjectSchema items = (JsonObjectSchema) ((JsonArraySchema) root.properties().get("values")).items();
        assertTrue(items.definitions().isEmpty());
        String reference = ((JsonReferenceSchema) ((JsonArraySchema) items.properties().get("children")).items()).reference();
        assertTrue(root.definitions().containsKey(reference));
    }

    @Test
    void typesSharingANameKeepTheReflectiveSchema() {
        Map<String, JsonSchema> schemas = schemasFor(
            Argument.of(PlannedDay.class),
            Argument.of(OtherStructuredOutputTypes.PlannedDay.class),
            Argument.of(PlannedTalk.class));

        assertEquals(Set.of("PlannedTalk"), schemas.keySet());
    }

    @Test
    void configuresAiServicesWithoutTransformerOrChatModel() {
        AiServiceContext aiServiceContext = AiServiceContext.create(Object.class);
        aiServiceContext.chatRequestTransformer = null;

        structuredOutputSchemas.configure(aiServiceContext, List.of(Argument.of(PlannedDay.class)));

        assertNull(aiServiceContext.chatModel);
        ChatRequest request = aiServiceContext.chatRequestTransformer.apply(requestFor("PlannedDay"), null);
        assertEquals("The talks planned for a day.", request.responseFormat().jsonSchema().rootElement().description());
        // requests without a provided schema are left as they are
        ChatRequest other = requestFor("Other");
        assertSame(other, aiServiceContext.chatRequestTransformer.apply(other, null));
        ChatRequest text = ChatRequest.builder().messages(UserMessage.from("hi")).build();
        assertSame(text, aiServiceContext.chatRequestTransformer.apply(text, null));
    }

    @Test
    void configuresAiServicesWhoseModelDeclaresTheCapability() {
        AiServiceContext aiServiceContext = AiServiceContext.create(Object.class);
        ChatModel chatModel = model(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA));
        aiServiceContext.chatModel = chatModel;

        structuredOutputSchemas.configure(aiServiceContext, List.of(Argument.of(PlannedDay.class)));
        structuredOutputSchemas.configure(aiServiceContext, List.of(Argument.STRING));

        assertSame(chatModel, aiServiceContext.chatModel);
        assertTrue(structuredOutputSchemas.declaresJsonSchemaCapability(chatModel));
        assertFalse(structuredOutputSchemas.declaresJsonSchemaCapability(model(Set.of())));
    }

    @Test
    void decoratesAgentModels() {
        ResponseFormatSchemas schemas = structuredOutputSchemas.schemasFor(List.of(Argument.of(PlannedDay.class))).orElseThrow();

        ChatModel decorated = structuredOutputSchemas.decorate(model(Set.of()), schemas, Object.class);

        assertInstanceOf(StructuredOutputChatModel.class, decorated);
        assertFalse(decorated.supportedCapabilities().contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA));
    }

    @Test
    void configuration() {
        StructuredOutputConfiguration configuration = new StructuredOutputConfiguration();
        assertTrue(configuration.isEnabled());
        assertNull(configuration.getForceJsonSchemaResponseFormat());

        configuration.setEnabled(false);
        configuration.setForceJsonSchemaResponseFormat(true);

        assertFalse(configuration.isEnabled());
        assertTrue(configuration.getForceJsonSchemaResponseFormat());
    }

    private Map<String, JsonSchema> schemasFor(Argument<?>... returnTypes) {
        return structuredOutputSchemas.schemasFor(List.of(returnTypes)).orElseThrow().schemas();
    }

    private static ChatRequest requestFor(String name) {
        return ChatRequest.builder()
            .messages(UserMessage.from("hi"))
            .responseFormat(ResponseFormat.builder()
                .type(ResponseFormatType.JSON)
                .jsonSchema(JsonSchema.builder().name(name).rootElement(JsonObjectSchema.builder().build()).build())
                .build())
            .build();
    }

    private static ChatModel model(Set<Capability> capabilities) {
        return new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest chatRequest) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Set<Capability> supportedCapabilities() {
                return capabilities;
            }
        };
    }
}
