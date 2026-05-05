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
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;

import java.util.List;

/**
 * Simple ChatModel used in tests.
 * It just echoes the last user text prefixed with a marker to keep behavior deterministic.
 */
@Singleton
@Primary
@Requires(env = "tools")
final class DefaultTestChatModel implements ChatModel {

    @Override
    public String chat(String userMessage) {
        return "[test-model] " + userMessage;
    }

    @Override
    public ChatResponse chat(ChatMessage... messages) {
        return chat(List.of(messages));
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(chat(allText(messages))))
            .build();
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(chat(allText(chatRequest.messages()))))
            .build();
    }

    private static String allText(List<ChatMessage> messages) {
        var sb = new StringBuilder();
        for (var m : messages) {
            try {
                var method = m.getClass().getMethod("text");
                var v = method.invoke(m);
                if (v != null) {
                    if (!sb.isEmpty()) {
                        sb.append('\n');
                    }
                    sb.append(v);
                }
            } catch (Exception _) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                sb.append(m.toString());
            }
        }
        return sb.toString();
    }
}
