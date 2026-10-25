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
package io.micronaut.langchain4j.agentic.rag;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Retrieval for agentic services is explicit: only {@code RetrievalAugmentor} and
 * {@code ContentRetriever} beans are attached, never an implicit embedding model and store pair.
 * The {@code tools} environment supplies a chat model that echoes every message it receives.
 */
class AgenticServiceRagTest {

    private static final String INJECTED_MARKER = "Answer using the following information:";

    @Test
    void embeddingModelAndStoreAreNotPairedImplicitly() {
        try (var ctx = ApplicationContext.run("tools", "rag-none")) {
            assertTrue(ctx.containsBean(EmbeddingModel.class));
            assertTrue(ctx.containsBean(EmbeddingStore.class));

            String answer = ctx.getBean(RagAgent.class).ask("What is the fact?");

            assertTrue(answer.contains("What is the fact?"), answer);
            assertFalse(answer.contains(INJECTED_MARKER), answer);
            assertEquals(0, ctx.getBean(CountingEmbeddingModel.class).embedCalls.get());
        }
    }

    @Test
    void contentRetrieverBeanIsAttached() {
        try (var ctx = ApplicationContext.run("tools", "rag-retriever")) {
            String answer = ctx.getBean(RagAgent.class).ask("What is the fact?");

            assertTrue(answer.contains("retrieved-fact"), answer);
            assertTrue(ctx.getBean(TestContentRetrievers.CountingContentRetriever.class).calls.get() >= 1);
        }
    }

    @Test
    void retrievalAugmentorBeanTakesPrecedenceOverContentRetriever() {
        try (var ctx = ApplicationContext.run("tools", "rag-augmentor")) {
            String answer = ctx.getBean(RagAgent.class).ask("What is the fact?");

            assertTrue(answer.contains("[augmented]"), answer);
            assertFalse(answer.contains("should-not-appear"), answer);
        }
    }

    @Test
    void retrieverNamedAfterAgentIsPreferred() {
        try (var ctx = ApplicationContext.run("tools", "rag-named")) {
            String answer = ctx.getBean(RagAgent.class).ask("What is the fact?");

            assertTrue(answer.contains("named-fact"), answer);
            assertFalse(answer.contains("default-fact"), answer);
        }
    }
}
