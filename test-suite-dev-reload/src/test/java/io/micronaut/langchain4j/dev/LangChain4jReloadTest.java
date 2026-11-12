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
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.interceptor.AiServiceInterceptor;
import io.micronaut.langchain4j.testutils.OllamaUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs applications that use LangChain4j through the development runtime and restarts them. A model whose HTTP client
 * holds nothing of the context is kept across a restart until a change under its {@code langchain4j} prefix releases
 * it; a model on the Micronaut HTTP client, which resolves its client through the context, is created again. The AI
 * services and tools of each generation are its own, and nothing of a retired generation stays reachable.
 */
class LangChain4jReloadTest {

    private static final String GREETER = """
        package example;

        @jakarta.inject.Singleton
        public class Greeter {
            public String greet() {
                return "%s";
            }
        }
        """;

    private static final String TOOLS = """
        package example;

        import dev.langchain4j.agent.tool.Tool;
        import jakarta.inject.Singleton;

        @Singleton
        public class Answers {
            @Tool("Answers the question")
            public String answer() {
                return "%s";
            }
        }
        """;

    private static final String ASSISTANT = """
        package example;

        import dev.langchain4j.service.SystemMessage;
        import io.micronaut.langchain4j.annotation.AiService;

        @AiService(tools = Answers.class)
        public interface Assistant {
            @SystemMessage(%s)
            String chat(String message);
        }
        """;

    private static String ollamaBaseUrl;

    @TempDir
    Path project;

    @BeforeAll
    static void startOllama() throws Exception {
        ollamaBaseUrl = OllamaUtils.ollamaContainerBaseUrl();
    }

    @Test
    void aModelOnAContextFreeHttpClientIsRetainedUntilItsConfigurationChanges() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            Map<String, String> configuration = ollama();
            configuration.put("test.http-client", "jdk");
            configuration.forEach(harness::property);
            harness.source("example.Greeter", GREETER.formatted("first"));
            harness.start();
            ChatModel model = harness.context().getBean(ChatModel.class);
            assertAnswers(model);

            harness.source("example.Greeter", GREETER.formatted("second"));
            harness.reload();
            assertEquals(2, harness.generation());
            ReloadTck.assertRetained(harness, model);
            assertSame(model, harness.context().getBean(ChatModel.class));
            assertAnswers(harness.context().getBean(ChatModel.class));

            // a change under langchain4j.ollama, together with a class, restarts the application and releases the model
            configuration.put("langchain4j.ollama.chat-model.temperature", "0.1");
            harness.resource("application.properties", properties(configuration));
            harness.source("example.Greeter", GREETER.formatted("third"));
            harness.reload();
            assertEquals(3, harness.generation());
            ChatModel released = harness.context().getBean(ChatModel.class);
            assertNotSame(model, released);
            assertAnswers(released);
            model = null;
            released = null;

            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void aModelOnTheMicronautHttpClientIsCreatedAgain() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            ollama().forEach(harness::property);
            harness.source("example.Greeter", GREETER.formatted("first"));
            harness.start();
            ChatModel model = harness.context().getBean(ChatModel.class);
            assertAnswers(model);

            harness.source("example.Greeter", GREETER.formatted("second"));
            harness.reload();
            assertEquals(2, harness.generation());
            // it would resolve its client through the stopped context
            ChatModel second = harness.context().getBean(ChatModel.class);
            assertNotSame(model, second);
            assertAnswers(second);
            model = null;
            second = null;

            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void theNextGenerationUsesItsOwnToolsAndAiServices() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            harness.property("test.chat-model", "scripted");
            harness.source("example.Answers", TOOLS.formatted("first"));
            harness.source("example.Assistant", ASSISTANT.formatted("\"system one\""));
            harness.start();
            assertEquals("first|system one", chat(harness.context(), "question"));

            harness.source("example.Answers", TOOLS.formatted("second"));
            harness.source("example.Assistant", ASSISTANT.formatted("\"system two\""));
            harness.reload();
            assertEquals(2, harness.generation());
            assertEquals("second|system two", chat(harness.context(), "question"));

            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void aPromptReadFromAResourceIsReadAgainOnTheNextCall() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            harness.property("test.chat-model", "scripted");
            harness.source("example.Answers", TOOLS.formatted("first"));
            harness.source("example.Assistant", ASSISTANT.formatted("fromResource = \"/prompts/system.txt\""));
            harness.resource("prompts/system.txt", "prompt one");
            harness.start();
            assertEquals("first|prompt one", chat(harness.context(), "question"));

            harness.resource("prompts/system.txt", "prompt two");
            harness.reload();
            assertEquals("first|prompt two", chat(harness.context(), "question"));
        }
    }

    @Test
    void aClassLoaderRetiredInPlaceRebuildsTheAiServices() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            harness.property("test.chat-model", "scripted");
            harness.source("example.Answers", TOOLS.formatted("first"));
            harness.source("example.Assistant", ASSISTANT.formatted("\"system one\""));
            harness.start();
            assertEquals("first|system one", chat(harness.context(), "question"));
            AiServiceInterceptor interceptor = harness.context().getBean(AiServiceInterceptor.class);
            Object assistant = harness.context().getBean(type(harness.context(), "example.Assistant"));

            retireLoaderInPlace(harness);

            assertEquals(1, harness.generation(), "the application did not restart");
            assertNotSame(interceptor, harness.context().getBean(AiServiceInterceptor.class), "the services it built are dropped");
            assertNotSame(assistant, harness.context().getBean(type(harness.context(), "example.Assistant")), "the AI service is created again");
            assertEquals("first|system one", chat(harness.context(), "question"));
        }
    }

    @Test
    void aChangedStoreFileRecreatesTheInMemoryEmbeddingStore() {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            harness.property("langchain4j.in-memory.embedding-stores.docs.path", "classpath:stores/docs.json");
            harness.resource("stores/docs.json", store("one"));
            harness.source("example.Greeter", GREETER.formatted("first"));
            harness.start();
            EmbeddingStore<?> store = harness.context().getBean(EmbeddingStore.class, Qualifiers.byName("docs"));
            assertEquals(1, matches(store));

            harness.resource("stores/docs.json", store("one", "two"));
            harness.reload();
            assertEquals(1, harness.generation(), "the application did not restart");
            EmbeddingStore<?> recreated = harness.context().getBean(EmbeddingStore.class, Qualifiers.byName("docs"));
            assertNotSame(store, recreated);
            assertEquals(2, matches(recreated));
        }
    }

    private static Map<String, String> ollama() {
        Map<String, String> configuration = new LinkedHashMap<>();
        configuration.put("langchain4j.ollama.base-url", ollamaBaseUrl);
        configuration.put("langchain4j.ollama.model-name", OllamaUtils.CHAT_MODEL_NAME);
        configuration.put("langchain4j.ollama.timeout", "120s");
        configuration.put("langchain4j.ollama.chat-model.num-predict", "8");
        return configuration;
    }

    private static String properties(Map<String, String> configuration) {
        StringBuilder properties = new StringBuilder();
        configuration.forEach((key, value) -> properties.append(key).append('=').append(value).append('\n'));
        return properties.toString();
    }

    private static void assertAnswers(ChatModel model) {
        String answer = model.chat("Say hello");
        assertFalse(answer == null || answer.isBlank(), "the model answers");
    }

    private static String store(String... texts) {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        for (String text : texts) {
            store.add(Embedding.from(new float[] {1, 0}), TextSegment.from(text));
        }
        return store.serializeToJson();
    }

    private static int matches(EmbeddingStore<?> store) {
        return store.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(Embedding.from(new float[] {1, 0}))
            .maxResults(10)
            .build()).matches().size();
    }

    /**
     * Tells the running generation that a reload applied in place retired a class loader, as the development runtime
     * does when it loads changed classes into a new one. The context is not kept: a reference to it would keep the
     * generation reachable.
     */
    private static void retireLoaderInPlace(ReloadHarness harness) {
        ApplicationContext context = harness.context();
        try (URLClassLoader retired = new URLClassLoader(new URL[0], null)) {
            context.publishEvent(new ClassChangeEvent(LangChain4jReloadTest.class, Set.of(retired), context.getClassLoader(),
                List.of(new ClassChange("example.Answers", ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        } catch (java.io.IOException e) {
            throw new AssertionError(e);
        }
    }

    private static String chat(ApplicationContext context, String message) {
        Object assistant = context.getBean(type(context, "example.Assistant"));
        try {
            return (String) type(context, "example.Assistant").getMethod("chat", String.class).invoke(assistant, message);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot call the assistant", e);
        }
    }

    private static Class<?> type(ApplicationContext context, String className) {
        try {
            return Class.forName(className, true, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(className + " is not in the application", e);
        }
    }
}
