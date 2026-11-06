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
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.langchain4j.agentic.AgentRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An agentic service is built on its first call and kept, so what it was built from has to build it again in place.
 */
class AgenticReloadTest {

    private static final String EXPERT = """
        package example;

        import dev.langchain4j.agentic.Agent;
        import dev.langchain4j.service.UserMessage;
        import dev.langchain4j.service.V;
        import io.micronaut.langchain4j.agentic.annotation.AgenticService;

        @AgenticService
        public interface Expert {
            @UserMessage("{{question}}")
            @Agent(description = "Answers the question")
            String ask(@V("question") String question);
        }
        """;

    private static final String RETRIEVAL = """
        package example;

        import dev.langchain4j.data.segment.TextSegment;
        import dev.langchain4j.model.embedding.EmbeddingModel;
        import dev.langchain4j.rag.content.retriever.ContentRetriever;
        import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
        import dev.langchain4j.store.embedding.EmbeddingStore;
        import io.micronaut.context.annotation.Factory;
        import jakarta.inject.Named;
        import jakarta.inject.Singleton;

        @Factory
        public class Retrieval {
            @Singleton
            ContentRetriever retriever(@Named("docs") EmbeddingStore<TextSegment> store, EmbeddingModel model) {
                return EmbeddingStoreContentRetriever.builder().embeddingStore(store).embeddingModel(model).maxResults(10).build();
            }
        }
        """;

    @TempDir
    Path project;

    @Test
    void aChangedStoreFileBuildsTheAgentAgainOnTheNewStore() {
        try (ReloadHarness harness = started()) {
            String first = ask(harness.context());
            assertTrue(first.contains("one"), first);
            assertFalse(first.contains("two"), first);

            harness.resource("stores/docs.json", store("one", "two"));
            harness.reload();

            assertEquals(1, harness.generation(), "the application did not restart");
            String second = ask(harness.context());
            assertTrue(second.contains("two"), second);
        }
    }

    @Test
    void aClassLoaderRetiredInPlaceBuildsTheAgentAgain() {
        try (ReloadHarness harness = started()) {
            ask(harness.context());
            Object registry = harness.context().getBean(AgentRegistry.class);

            retireLoaderInPlace(harness.context());

            assertEquals(1, harness.generation(), "the application did not restart");
            assertNotSame(registry, harness.context().getBean(AgentRegistry.class), "the agents it built are dropped");
            assertTrue(ask(harness.context()).contains("one"));
        }
    }

    private ReloadHarness started() {
        ReloadHarness harness = ReloadHarness.inDirectory(project);
        harness.property("test.chat-model", "echo");
        harness.property("langchain4j.in-memory.embedding-stores.docs.path", "classpath:stores/docs.json");
        harness.resource("stores/docs.json", store("one"));
        harness.source("example.Expert", EXPERT);
        harness.source("example.Retrieval", RETRIEVAL);
        harness.start();
        return harness;
    }

    private static String store(String... texts) {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        for (String text : texts) {
            store.add(Embedding.from(new float[] {1, 0}), TextSegment.from(text));
        }
        return store.serializeToJson();
    }

    private static void retireLoaderInPlace(ApplicationContext context) {
        try (URLClassLoader retired = new URLClassLoader(new URL[0], null)) {
            context.publishEvent(new ClassChangeEvent(AgenticReloadTest.class, Set.of(retired), context.getClassLoader(),
                List.of(new ClassChange("example.Expert", ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String ask(ApplicationContext context) {
        try {
            Class<?> type = Class.forName("example.Expert", true, context.getClassLoader());
            return (String) type.getMethod("ask", String.class).invoke(context.getBean(type), "question");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot call the agent", e);
        }
    }
}
