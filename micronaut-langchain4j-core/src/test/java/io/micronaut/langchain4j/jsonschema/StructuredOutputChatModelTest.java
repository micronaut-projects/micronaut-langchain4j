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
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.ChatRequestOptions;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructuredOutputChatModelTest {

    private static final ChatModelListener LISTENER = new ChatModelListener() {
    };
    private static final ChatRequestParameters PARAMETERS = ChatRequestParameters.builder().temperature(0.5).build();

    private final List<String> calls = new ArrayList<>();
    private final ChatModel delegate = new ChatModel() {
        @Override
        public ChatResponse chat(ChatRequest chatRequest) {
            return recordCall("chat", chatRequest);
        }

        @Override
        public ChatResponse chat(ChatRequest chatRequest, ChatRequestOptions options) {
            return recordCall("chatWithOptions", chatRequest);
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            return recordCall("doChat", chatRequest);
        }

        @Override
        public CompletableFuture<ChatResponse> chatAsync(ChatRequest chatRequest) {
            return CompletableFuture.completedFuture(recordCall("chatAsync", chatRequest));
        }

        @Override
        public CompletableFuture<ChatResponse> chatAsync(ChatRequest chatRequest, ChatRequestOptions options) {
            return CompletableFuture.completedFuture(recordCall("chatAsyncWithOptions", chatRequest));
        }

        @Override
        public CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
            return CompletableFuture.completedFuture(recordCall("doChatAsync", chatRequest));
        }

        @Override
        public ChatRequestParameters defaultRequestParameters() {
            return PARAMETERS;
        }

        @Override
        public List<ChatModelListener> listeners() {
            return List.of(LISTENER);
        }

        @Override
        public ModelProvider provider() {
            return ModelProvider.OTHER;
        }

        @Override
        public Set<Capability> supportedCapabilities() {
            return Set.of();
        }
    };

    @Test
    void delegatesWithRewrittenRequests() {
        UnaryOperator<ChatRequest> rewriter = request -> ChatRequest.builder().messages(UserMessage.from("rewritten")).build();
        StructuredOutputChatModel model = new StructuredOutputChatModel(delegate, rewriter, true);
        ChatRequest request = ChatRequest.builder().messages(UserMessage.from("original")).build();

        model.chat(request);
        model.chat(request, ChatRequestOptions.EMPTY);
        model.doChat(request);
        model.chatAsync(request).join();
        model.chatAsync(request, ChatRequestOptions.EMPTY).join();
        model.doChatAsync(request).join();

        assertEquals(List.of("chat:rewritten", "chatWithOptions:rewritten", "doChat:rewritten",
            "chatAsync:rewritten", "chatAsyncWithOptions:rewritten", "doChatAsync:rewritten"), calls);
        assertSame(PARAMETERS, model.defaultRequestParameters());
        assertEquals(List.of(LISTENER), model.listeners());
        assertEquals(ModelProvider.OTHER, model.provider());
        assertSame(delegate, model.delegate());
        assertTrue(model.toString().contains(delegate.toString()));
    }

    @Test
    void declaresTheJsonSchemaCapabilityOnlyWhenAsked() {
        assertEquals(Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA),
            new StructuredOutputChatModel(delegate, UnaryOperator.identity(), true).supportedCapabilities());
        assertEquals(Set.of(), new StructuredOutputChatModel(delegate, UnaryOperator.identity(), false).supportedCapabilities());
    }

    private ChatResponse recordCall(String method, ChatRequest request) {
        calls.add(method + ":" + ((UserMessage) request.messages().getFirst()).singleText());
        return ChatResponse.builder().aiMessage(AiMessage.from("ok")).build();
    }
}
