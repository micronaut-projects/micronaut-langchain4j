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
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Chat model test double that records the last request it received.
 */
final class RecordingChatModel implements ChatModel {

    private final AtomicReference<ChatRequest> lastRequest = new AtomicReference<>();

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        lastRequest.set(chatRequest);
        return ChatResponse.builder().aiMessage(new AiMessage("ok")).build();
    }

    ChatRequest lastRequest() {
        return lastRequest.get();
    }

    List<ChatMessage> lastMessages() {
        return lastRequest().messages();
    }

    String lastUserText() {
        return ((UserMessage) lastMessages().getLast()).singleText();
    }
}
