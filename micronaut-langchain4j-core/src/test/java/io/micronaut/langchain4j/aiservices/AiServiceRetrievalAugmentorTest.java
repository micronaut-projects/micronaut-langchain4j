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

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * A {@link RetrievalAugmentor} bean is attached and takes precedence over a {@link ContentRetriever} bean.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceRetrievalAugmentorTest.SPEC_NAME)
class AiServiceRetrievalAugmentorTest {

    static final String SPEC_NAME = "AiServiceRetrievalAugmentorTest";

    @Inject
    RecordingChatModel chatModel;

    @Inject
    CountingRetrievalAugmentor retrievalAugmentor;

    @Inject
    CountingContentRetriever contentRetriever;

    @Test
    void retrievalAugmentorBeanTakesPrecedence(Assistant assistant) {
        assertEquals("ok", assistant.chat("hello"));

        assertEquals(1, retrievalAugmentor.calls.get());
        assertEquals(0, contentRetriever.calls.get());
        String userText = chatModel.lastUserText();
        assertEquals("hello [augmented]", userText);
        assertFalse(userText.contains("Answer using the following information:"), userText);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Assistant {
        String chat(String userMessage);
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class CountingRetrievalAugmentor implements RetrievalAugmentor {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public AugmentationResult augment(AugmentationRequest augmentationRequest) {
            calls.incrementAndGet();
            String text = ((UserMessage) augmentationRequest.chatMessage()).singleText();
            return new AugmentationResult(UserMessage.from(text + " [augmented]"), List.of());
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class CountingContentRetriever implements ContentRetriever {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<Content> retrieve(Query query) {
            calls.incrementAndGet();
            return List.of(Content.from("should not appear"));
        }
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestBeans {

        @Singleton
        RecordingChatModel chatModel() {
            return new RecordingChatModel();
        }
    }
}
