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

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.ChatRequestOptions;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.core.annotation.Internal;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.UnaryOperator;

/**
 * Decorates the {@link ChatModel} of one AI service or agent: it optionally declares
 * {@link Capability#RESPONSE_FORMAT_JSON_SCHEMA}, so that LangChain4j sends a response format instead of format
 * instructions, and optionally rewrites the requests, where LangChain4j offers no request transformer (agents).
 */
@Internal
final class StructuredOutputChatModel implements ChatModel {

    private final ChatModel delegate;
    private final UnaryOperator<ChatRequest> requestRewriter;
    private final boolean declareJsonSchemaCapability;

    StructuredOutputChatModel(ChatModel delegate, UnaryOperator<ChatRequest> requestRewriter, boolean declareJsonSchemaCapability) {
        this.delegate = delegate;
        this.requestRewriter = requestRewriter;
        this.declareJsonSchemaCapability = declareJsonSchemaCapability;
    }

    ChatModel delegate() {
        return delegate;
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return delegate.chat(requestRewriter.apply(chatRequest));
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest, ChatRequestOptions options) {
        return delegate.chat(requestRewriter.apply(chatRequest), options);
    }

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        return delegate.doChat(requestRewriter.apply(chatRequest));
    }

    @Override
    public CompletableFuture<ChatResponse> chatAsync(ChatRequest chatRequest) {
        return delegate.chatAsync(requestRewriter.apply(chatRequest));
    }

    @Override
    public CompletableFuture<ChatResponse> chatAsync(ChatRequest chatRequest, ChatRequestOptions options) {
        return delegate.chatAsync(requestRewriter.apply(chatRequest), options);
    }

    @Override
    public CompletableFuture<ChatResponse> doChatAsync(ChatRequest chatRequest) {
        return delegate.doChatAsync(requestRewriter.apply(chatRequest));
    }

    @Override
    public ChatRequestParameters defaultRequestParameters() {
        return delegate.defaultRequestParameters();
    }

    @Override
    public List<ChatModelListener> listeners() {
        return delegate.listeners();
    }

    @Override
    public ModelProvider provider() {
        return delegate.provider();
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        Set<Capability> capabilities = delegate.supportedCapabilities();
        if (!declareJsonSchemaCapability || capabilities.contains(Capability.RESPONSE_FORMAT_JSON_SCHEMA)) {
            return capabilities;
        }
        Set<Capability> declared = new HashSet<>(capabilities);
        declared.add(Capability.RESPONSE_FORMAT_JSON_SCHEMA);
        return declared;
    }

    @Override
    public String toString() {
        return "StructuredOutputChatModel[" + delegate + "]";
    }
}
