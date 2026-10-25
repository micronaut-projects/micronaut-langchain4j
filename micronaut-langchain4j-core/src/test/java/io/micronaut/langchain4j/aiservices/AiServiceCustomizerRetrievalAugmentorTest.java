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
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.RetrievalAugmentor;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A customizer can install a {@link RetrievalAugmentor}, which was impossible while the factory
 * attached a content retriever before running the customizer.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceCustomizerRetrievalAugmentorTest.SPEC_NAME)
class AiServiceCustomizerRetrievalAugmentorTest {

    static final String SPEC_NAME = "AiServiceCustomizerRetrievalAugmentorTest";

    @Inject
    RecordingChatModel chatModel;

    @Test
    void customizerCanSetRetrievalAugmentor(Assistant assistant) {
        assertDoesNotThrow(() -> assistant.chat("hello"));
        assertEquals("hello [augmented]", chatModel.lastUserText());
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

        @Singleton
        AiServiceCustomizer<Assistant> customizer() {
            return context -> context.aiServices().retrievalAugmentor(request -> new AugmentationResult(
                UserMessage.from(((UserMessage) request.chatMessage()).singleText() + " [augmented]"),
                List.of()));
        }
    }
}
