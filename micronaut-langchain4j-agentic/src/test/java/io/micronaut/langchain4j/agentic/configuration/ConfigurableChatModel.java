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
package io.micronaut.langchain4j.agentic.configuration;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Requires;

import java.util.Arrays;
import java.util.List;

/**
 * Test ChatModel that returns different responses based on its name.
 */
public abstract class ConfigurableChatModel implements ChatModel {

    private final String name;

    protected ConfigurableChatModel(String name) {
        this.name = name;
    }

    @Override
    public String chat(String userMessage) {
        return "[" + name + "] " + generateResponse(userMessage);
    }

    protected abstract String generateResponse(String userMessage);

    @Override
    public ChatResponse chat(ChatMessage... messages) {
        var input = allText(Arrays.asList(messages));
        return ChatResponse.builder()
                .aiMessage(AiMessage.from(chat(input)))
                .build();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        var input = allText(messages);
        return ChatResponse.builder()
                .aiMessage(AiMessage.from(chat(input)))
                .build();
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        var input = allText(chatRequest.messages());
        return ChatResponse.builder()
                .aiMessage(AiMessage.from(chat(input)))
                .build();
    }


    @Singleton
    @Named("friendlyChatModel")
    @Requires(env = "alternate")
    public static class FriendlyChatModelCamelCase extends ConfigurableChatModel {
        public FriendlyChatModelCamelCase() {
            super("friendly");
        }

        @Override
        protected String generateResponse(String userMessage) {
            if (userMessage.contains("greet")) {
                return "Hello there! How can I help you today?";
            } else if (userMessage.contains("write")) {
                return "I'd be happy to help you write something creative!";
            } else {
                return "That's interesting! Tell me more.";
            }
        }
    }

    private static String allText(List<ChatMessage> messages) {
        var sb = new StringBuilder();
        for (var m : messages) {
            var t = messageText(m);
            if (t != null && !t.isEmpty()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(t);
            }
        }
        return sb.toString();
    }

    private static String messageText(ChatMessage message) {
        try {
            var m = message.getClass().getMethod("text");
            var v = m.invoke(message);
            if (v != null) {
                return v.toString();
            }
        } catch (Exception _) {
            // Fall through to toString()
        }
        return message.toString();
    }

    @Singleton
    @Named("professionalChatModel")
    @Requires(env = "alternate")
    public static class ProfessionalChatModelCamelCase extends ConfigurableChatModel {
        public ProfessionalChatModelCamelCase() {
            super("professional");
        }

        @Override
        protected String generateResponse(String userMessage) {
            if (userMessage.contains("greet")) {
                return "Greetings. How may I assist you?";
            } else if (userMessage.contains("write")) {
                return "I will assist with your writing requirements.";
            } else {
                return "Understood. Please provide additional details.";
            }
        }
    }


    @Singleton
    @Named("creativeChatModel")
    @Requires(env = "alternate")
    public static class CreativeChatModelCamelCase extends ConfigurableChatModel {
        public CreativeChatModelCamelCase() {
            super("creative");
        }

        @Override
        protected String generateResponse(String userMessage) {
            return "Let's do something creative: " + userMessage;
        }
    }
}
