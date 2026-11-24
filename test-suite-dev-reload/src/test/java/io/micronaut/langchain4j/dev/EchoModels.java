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
package io.micronaut.langchain4j.dev;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Models of the test classpath, which the development runtime treats as part of the runtime classpath: the chat model
 * answers with the user message it received, which shows the content an agent retrieved, and every text has the same
 * embedding.
 */
@Factory
@Requires(property = "test.chat-model", value = "echo")
public class EchoModels {

    @Singleton
    @Bean(typed = ChatModel.class)
    ChatModel chatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse doChat(ChatRequest request) {
                List<ChatMessage> messages = request.messages();
                UserMessage last = (UserMessage) messages.getLast();
                return ChatResponse.builder().aiMessage(AiMessage.from(last.singleText())).build();
            }
        };
    }

    @Singleton
    @Bean(typed = EmbeddingModel.class)
    EmbeddingModel embeddingModel() {
        return new EmbeddingModel() {
            @Override
            public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
                return Response.from(segments.stream().map(segment -> Embedding.from(new float[] {1, 0})).toList());
            }
        };
    }
}
