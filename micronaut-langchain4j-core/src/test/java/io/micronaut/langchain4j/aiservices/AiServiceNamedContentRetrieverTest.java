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
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A {@link ContentRetriever} bean named after the service is preferred over the default bean.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AiServiceNamedContentRetrieverTest.SPEC_NAME)
class AiServiceNamedContentRetrieverTest {

    static final String SPEC_NAME = "AiServiceNamedContentRetrieverTest";

    @Inject
    RecordingChatModel chatModel;

    @Test
    void namedServiceUsesNamedRetriever(ExpertAssistant expert) {
        assertEquals("ok", expert.chat("question"));
        String userText = chatModel.lastUserText();
        assertTrue(userText.contains("NAMED"), userText);
        assertFalse(userText.contains("DEFAULT"), userText);
    }

    @Test
    void plainServiceUsesDefaultRetriever(PlainAssistant plain) {
        assertEquals("ok", plain.chat("question"));
        String userText = chatModel.lastUserText();
        assertTrue(userText.contains("DEFAULT"), userText);
        assertFalse(userText.contains("NAMED"), userText);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService("expert")
    interface ExpertAssistant {
        String chat(String userMessage);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface PlainAssistant {
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
        @Named("expert")
        ContentRetriever expertRetriever() {
            return query -> List.of(Content.from("NAMED"));
        }

        @Singleton
        @Primary
        ContentRetriever defaultRetriever() {
            return query -> List.of(Content.from("DEFAULT"));
        }
    }
}
