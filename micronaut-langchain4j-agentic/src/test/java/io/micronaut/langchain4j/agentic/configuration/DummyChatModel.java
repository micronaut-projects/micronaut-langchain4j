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

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;
import io.micronaut.context.annotation.Requires;

import java.util.List;

/**
 * Minimal ChatModel used for tests. It is not intended to be called,
 * only to satisfy bean wiring for typed agent construction.
 */
@Singleton
@Requires(env = "dummy")
public class DummyChatModel implements ChatModel {

    @Override
    public String chat(String userMessage) {
        return "dummy";
    }

    @Override
    public ChatResponse chat(ChatMessage... messages) {
        return null;
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return null;
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return null;
    }
}
