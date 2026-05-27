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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "DisabledInMemoryAiServiceMemoryIdTest")
@Property(name = "langchain4j.chat-memory-store.inmemory.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class DisabledInMemoryAiServiceMemoryIdTest {

    @Inject
    Assistant assistant;

    @Test
    void aiServiceWithMemoryIdFailsWhenInMemoryChatMemoryIsDisabled() {
        var ex = assertThrows(IllegalConfigurationException.class,
            () -> assistant.chat(UUID.randomUUID().toString(), "hello"));
        assertTrue(ex.getMessage().contains("In order to use @MemoryId, please configure the ChatMemoryProvider"),
            "Expected exception message to mention missing ChatMemoryProvider, but was: " + ex.getMessage());
    }

    @Requires(property = "spec.name", value = "DisabledInMemoryAiServiceMemoryIdTest")
    @AiService
    interface Assistant {
        String chat(@MemoryId String memoryId, @UserMessage String message);
    }

    @Factory
    @Requires(property = "spec.name", value = "DisabledInMemoryAiServiceMemoryIdTest")
    static final class TestFactory {

        @Bean
        @Singleton
        ChatModel chatModel() {
            return new ChatModel() {
                @Override
                public ChatResponse chat(List<dev.langchain4j.data.message.ChatMessage> messages) {
                    return ChatResponse.builder()
                        .aiMessage(AiMessage.from("stub"))
                        .build();
                }
            };
        }
    }
}
