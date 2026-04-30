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
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.service.TokenStream;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceFactoryModelSelectionTest.SPEC_NAME)
class AiServiceFactoryModelSelectionTest {

    static final String SPEC_NAME = "AiServiceFactoryModelSelectionTest";

    @Inject
    BeanContext beanContext;

    @Test
    void configuresOnlyChatModelForNonStreamingServices(ChatAssistant assistant) {
        assertEquals("chat-model", assistant.chat("hello"));
        assertEquals(AiServiceFactory.ModelSelection.CHAT, AiServiceFactory.selectModels(beanContext.getBeanDefinition(ChatAssistant.class)));
    }

    @Test
    void configuresOnlyStreamingChatModelForStreamingServices(StreamingAssistant assistant) throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<String> partialResponse = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        assistant.chat("hello")
            .onPartialResponse(partialResponse::set)
            .onCompleteResponse(ignore -> completed.countDown())
            .onError(error -> {
                errorRef.set(error);
                completed.countDown();
            })
            .start();

        assertTrue(completed.await(5, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            throw new AssertionError(errorRef.get());
        }
        assertEquals("streaming-model", partialResponse.get());
        assertEquals(AiServiceFactory.ModelSelection.STREAMING, AiServiceFactory.selectModels(beanContext.getBeanDefinition(StreamingAssistant.class)));
    }

    @Test
    void configuresBothModelsForMixedServices(MixedAssistant assistant) throws InterruptedException {
        assertEquals("chat-model", assistant.chat("hello"));

        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<String> partialResponse = new AtomicReference<>();
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        assistant.streamChat("hello")
            .onPartialResponse(partialResponse::set)
            .onCompleteResponse(ignore -> completed.countDown())
            .onError(error -> {
                errorRef.set(error);
                completed.countDown();
            })
            .start();

        assertTrue(completed.await(5, TimeUnit.SECONDS));
        if (errorRef.get() != null) {
            throw new AssertionError(errorRef.get());
        }
        assertEquals("streaming-model", partialResponse.get());
        assertEquals(AiServiceFactory.ModelSelection.BOTH, AiServiceFactory.selectModels(beanContext.getBeanDefinition(MixedAssistant.class)));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface ChatAssistant {
        String chat(String userMessage);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface StreamingAssistant {
        TokenStream chat(String userMessage);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface MixedAssistant {
        String chat(String userMessage);
        TokenStream streamChat(String userMessage);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestModelsFactory {

        @Singleton
        ChatModel chatModel() {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest chatRequest) {
                    return ChatResponse.builder()
                        .aiMessage(new AiMessage("chat-model"))
                        .build();
                }
            };
        }

        @Singleton
        StreamingChatModel streamingChatModel() {
            return new StreamingChatModel() {
                @Override
                public void doChat(ChatRequest chatRequest, StreamingChatResponseHandler handler) {
                    assertNotNull(chatRequest);
                    handler.onPartialResponse("streaming-model");
                    handler.onCompleteResponse(ChatResponse.builder()
                        .aiMessage(new AiMessage("streaming-model"))
                        .build());
                }
            };
        }
    }
}
