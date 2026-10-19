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
package io.micronaut.langchain4j.test;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.micronaut.core.annotation.Experimental;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A chat model for tests that answers each request with the first matching rule, and records the requests.
 *
 * <pre>{@code
 * ScriptedChatModel model = ScriptedChatModel.builder()
 *     .whenToolResult().respond(request -> "It is " + ScriptedChatModel.lastToolResult(request))
 *     .whenUserMessageContains("weather").callTool("weather", "{\"city\": \"Paris\"}")
 *     .otherwise("I don't know")
 *     .build();
 * }</pre>
 *
 * <p>{@link #streaming()} returns a {@link StreamingChatModel} with the same rules, which streams the text of the
 * response word by word.</p>
 *
 * @since 2.4.0
 */
@Experimental
public final class ScriptedChatModel implements ChatModel {

    private final List<Rule> rules;
    private final List<ChatRequest> requests = new CopyOnWriteArrayList<>();

    private ScriptedChatModel(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * @return A builder of the rules of the model
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A model that answers the requests with the given responses, in order, and then with the last one.
     *
     * @param responses The responses
     * @return The model
     */
    public static ScriptedChatModel respondingWith(String... responses) {
        if (responses.length == 0) {
            throw new IllegalArgumentException("At least one response is required");
        }
        AtomicInteger index = new AtomicInteger();
        return builder()
            .otherwise(request -> responses[Math.min(index.getAndIncrement(), responses.length - 1)])
            .build();
    }

    /**
     * @param request A chat request
     * @return The text of the last user message of the request, or an empty string
     */
    public static String lastUserMessage(ChatRequest request) {
        List<ChatMessage> messages = request.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i) instanceof UserMessage user) {
                return user.hasSingleText() ? user.singleText() : user.contents().toString();
            }
        }
        return "";
    }

    /**
     * @param request A chat request
     * @return The result of the tool execution that ends the request, or an empty string
     */
    public static String lastToolResult(ChatRequest request) {
        return request.messages().getLast() instanceof ToolExecutionResultMessage result ? result.text() : "";
    }

    /**
     * @return The requests received, in order
     */
    public List<ChatRequest> requests() {
        return Collections.unmodifiableList(requests);
    }

    /**
     * @return The last request received
     */
    public ChatRequest lastRequest() {
        if (requests.isEmpty()) {
            throw new IllegalStateException("No request received");
        }
        return requests.getLast();
    }

    /**
     * Forgets the requests received.
     */
    public void reset() {
        requests.clear();
    }

    @Override
    public ChatResponse doChat(ChatRequest request) {
        requests.add(request);
        for (Rule rule : rules) {
            if (rule.condition().test(request)) {
                return ChatResponse.builder().aiMessage(rule.answer().apply(request)).build();
            }
        }
        throw new IllegalStateException("No rule of the scripted chat model matches the request: " + lastUserMessage(request));
    }

    /**
     * @return A streaming chat model with the same rules and requests, which streams the text word by word
     */
    public StreamingChatModel streaming() {
        return new StreamingChatModel() {
            @Override
            public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                stream(request, handler);
            }
        };
    }

    private void stream(ChatRequest request, StreamingChatResponseHandler handler) {
        ChatResponse response;
        try {
            response = doChat(request);
        } catch (RuntimeException e) {
            handler.onError(e);
            return;
        }
        String text = response.aiMessage().text();
        if (text != null) {
            for (String token : text.split("(?<=\\s)")) {
                handler.onPartialResponse(token);
            }
        }
        handler.onCompleteResponse(response);
    }

    /**
     * Builds the rules of a {@link ScriptedChatModel}, evaluated in order.
     */
    public static final class Builder {

        private final List<Rule> rules = new ArrayList<>();

        private Builder() {
        }

        /**
         * @param condition The condition of the rule
         * @return The answer of the rule
         */
        public Answer when(Predicate<ChatRequest> condition) {
            return new Answer(this, condition);
        }

        /**
         * @param text The text to look for
         * @return The answer to the requests whose last user message contains the text
         */
        public Answer whenUserMessageContains(String text) {
            return when(request -> lastUserMessage(request).contains(text));
        }

        /**
         * @return The answer to the requests that end with the result of a tool execution
         */
        public Answer whenToolResult() {
            return when(request -> request.messages().getLast() instanceof ToolExecutionResultMessage);
        }

        /**
         * @param response The response to the requests that match no other rule
         * @return The builder
         */
        public Builder otherwise(String response) {
            return when(request -> true).respond(response);
        }

        /**
         * @param response The response to the requests that match no other rule
         * @return The builder
         */
        public Builder otherwise(Function<ChatRequest, String> response) {
            return when(request -> true).respond(response);
        }

        /**
         * @return The chat model
         */
        public ScriptedChatModel build() {
            return new ScriptedChatModel(rules);
        }
    }

    /**
     * The answer of a rule.
     */
    public static final class Answer {

        private final Builder builder;
        private final Predicate<ChatRequest> condition;

        private Answer(Builder builder, Predicate<ChatRequest> condition) {
            this.builder = builder;
            this.condition = condition;
        }

        /**
         * @param response The text of the response
         * @return The builder
         */
        public Builder respond(String response) {
            return respond(request -> response);
        }

        /**
         * @param response The text of the response, from the request
         * @return The builder
         */
        public Builder respond(Function<ChatRequest, String> response) {
            builder.rules.add(new Rule(condition, request -> AiMessage.from(response.apply(request))));
            return builder;
        }

        /**
         * @param toolName The name of the tool to execute
         * @param arguments The arguments of the tool, as JSON
         * @return The builder
         */
        public Builder callTool(String toolName, String arguments) {
            AtomicInteger ids = new AtomicInteger();
            builder.rules.add(new Rule(condition, request -> AiMessage.from(ToolExecutionRequest.builder()
                .id(toolName + "-" + ids.incrementAndGet())
                .name(toolName)
                .arguments(arguments)
                .build())));
            return builder;
        }
    }

    private record Rule(Predicate<ChatRequest> condition, Function<ChatRequest, AiMessage> answer) {
    }
}
