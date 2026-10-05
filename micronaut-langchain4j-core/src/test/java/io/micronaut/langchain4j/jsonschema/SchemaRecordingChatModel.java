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

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chat model test double that records the last request and answers with the JSON registered for the name of the
 * requested schema.
 */
final class SchemaRecordingChatModel implements ChatModel {

    private final AtomicReference<ChatRequest> lastRequest = new AtomicReference<>();
    private final Set<Capability> capabilities;
    private final Map<String, String> answers;

    SchemaRecordingChatModel(Set<Capability> capabilities, Map<String, String> answers) {
        this.capabilities = capabilities.isEmpty() ? EnumSet.noneOf(Capability.class) : EnumSet.copyOf(capabilities);
        this.answers = answers;
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        lastRequest.set(chatRequest);
        String answer = chatRequest.responseFormat() != null && chatRequest.responseFormat().jsonSchema() != null
            ? answers.get(chatRequest.responseFormat().jsonSchema().name())
            : answers.values().iterator().next();
        return ChatResponse.builder().aiMessage(new AiMessage(answer)).build();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        return capabilities;
    }

    ChatRequest lastRequest() {
        return lastRequest.get();
    }
}
