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
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An {@code @AiService} that declares no retrieval must not be paired with the
 * embedding model and embedding store beans that happen to exist (GitHub issue 383).
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceNoImplicitRetrievalTest.SPEC_NAME)
class AiServiceNoImplicitRetrievalTest {

    static final String SPEC_NAME = "AiServiceNoImplicitRetrievalTest";

    @Inject
    BeanContext beanContext;

    @Inject
    RecordingChatModel chatModel;

    @Inject
    CountingEmbeddingModel embeddingModel;

    @Test
    void plainServiceDoesNotRetrieve(Assistant assistant) {
        assertTrue(beanContext.containsBean(EmbeddingModel.class));
        assertTrue(beanContext.containsBean(EmbeddingStore.class));

        assertEquals("ok", assistant.chat("hello"));

        assertEquals(0, embeddingModel.embedCalls());
        assertEquals(1, chatModel.lastMessages().size());
        assertInstanceOf(UserMessage.class, chatModel.lastMessages().getFirst());
        assertEquals("hello", chatModel.lastUserText());
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Assistant {
        String chat(String userMessage);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestBeans {

        @Singleton
        RecordingChatModel chatModel() {
            return new RecordingChatModel();
        }

        @Singleton
        CountingEmbeddingModel embeddingModel() {
            return new CountingEmbeddingModel();
        }
    }
}
