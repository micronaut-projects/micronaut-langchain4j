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

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@link ContentRetriever} bean is attached to the service.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceContentRetrieverTest.SPEC_NAME)
class AiServiceContentRetrieverTest {

    static final String SPEC_NAME = "AiServiceContentRetrieverTest";

    @Inject
    RecordingChatModel chatModel;

    @Inject
    CountingContentRetriever contentRetriever;

    @Test
    void contentRetrieverBeanIsAttached(Assistant assistant) {
        assertEquals("ok", assistant.chat("Who created Micronaut?"));

        assertEquals(1, contentRetriever.calls.get());
        String userText = chatModel.lastUserText();
        assertTrue(userText.startsWith("Who created Micronaut?"), userText);
        assertTrue(userText.contains("Answer using the following information:"), userText);
        assertTrue(userText.contains("Micronaut was created by Graeme Rocher"), userText);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Assistant {
        String chat(String userMessage);
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class CountingContentRetriever implements ContentRetriever {

        final AtomicInteger calls = new AtomicInteger();

        @Override
        public List<Content> retrieve(dev.langchain4j.rag.query.Query query) {
            calls.incrementAndGet();
            return List.of(Content.from("Micronaut was created by Graeme Rocher"));
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
