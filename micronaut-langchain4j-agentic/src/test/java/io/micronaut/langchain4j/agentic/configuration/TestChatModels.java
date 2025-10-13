/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.agentic.configuration;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Provides named ChatModel beans for property-based AgenticService tests in the 'alternate' environment.
 */
@Requires(env = "alternate")
public final class TestChatModels {

    /**
     * ChatModel which inspects the conversation history and includes information
     * about the previous turn in the response.
     *
     * This is used to validate that Agentic services created through Micronaut
     * receive a configured ChatMemory and send message history to the model.
     */
    @Singleton
    @Named("memory-aware-chat-model")
    @Requires(env = "alternate")
    public static class MemoryAwareChatModel implements ChatModel {

        @Override
        public String chat(String userMessage) {
            // For direct string calls, there's no message history provided by LC4J,
            // so simply prefix a marker for consistency.
            return "[mem] " + userMessage;
        }

        @Override
        public ChatResponse chat(ChatMessage... messages) {
            return chat(List.of(messages));
        }

        @Override
        public ChatResponse chat(List<ChatMessage> messages) {
            var out = buildWithHistory(messages);
            return ChatResponse.builder()
                .aiMessage(AiMessage.from(out))
                .build();
        }

        @Override
        public ChatResponse chat(ChatRequest chatRequest) {
            var out = buildWithHistory(chatRequest.messages());
            return ChatResponse.builder()
                .aiMessage(AiMessage.from(out))
                .build();
        }

        private String buildWithHistory(List<ChatMessage> messages) {
            var size = messages != null ? messages.size() : 0;
            String prevUser = null;
            if (size >= 2) {
                // Find the previous user message before the last message
                for (var i = size - 2; i >= 0; i--) {
                    var m = messages.get(i);
                    if (m instanceof UserMessage) {
                        var um = (UserMessage) m;
                        var text = um.toString();
                        prevUser = text;
                        break;
                    }
                }
            }
            var sb = new StringBuilder();
            sb.append("[mem] history=").append(size);
            if (prevUser != null) {
                sb.append(" prevUser=").append(prevUser);
            }
            return sb.toString();
        }

    }
}
