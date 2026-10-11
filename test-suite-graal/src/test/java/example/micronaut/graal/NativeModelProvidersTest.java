package example.micronaut.graal;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Calls the chat and embedding models of the providers in a native image, against {@link FakeProviderServer}: the
 * providers' HTTP clients and request/response mapping must work without hand-written reflection configuration.
 */
class NativeModelProvidersTest {

    private static FakeProviderServer server;
    private static ApplicationContext context;

    @BeforeAll
    static void start() {
        server = new FakeProviderServer();
        context = ApplicationContext.run(Map.ofEntries(
            Map.entry("spec.name", "NativeModelProvidersTest"),
            Map.entry("langchain4j.open-ai.api-key", "test"),
            Map.entry("langchain4j.open-ai.base-url", server.url("openai/")),
            Map.entry("langchain4j.open-ai.chat-models.openai.model-name", "test"),
            Map.entry("langchain4j.open-ai.embedding-models.openai.model-name", "test"),
            Map.entry("langchain4j.mistral-ai.api-key", "test"),
            Map.entry("langchain4j.mistral-ai.base-url", server.url("mistral/")),
            Map.entry("langchain4j.mistral-ai.chat-models.mistral.model-name", "test"),
            Map.entry("langchain4j.mistral-ai.embedding-models.mistral.model-name", "test"),
            Map.entry("langchain4j.anthropic.api-key", "test"),
            Map.entry("langchain4j.anthropic.base-url", server.url("anthropic/")),
            Map.entry("langchain4j.anthropic.chat-models.anthropic.model-name", "test"),
            Map.entry("langchain4j.ollama.base-url", server.url("ollama")),
            Map.entry("langchain4j.ollama.chat-models.ollama.model-name", "test"),
            Map.entry("langchain4j.ollama.embedding-models.ollama.model-name", "test"),
            Map.entry("langchain4j.google-ai-gemini.api-key", "test"),
            Map.entry("langchain4j.google-ai-gemini.chat-models.gemini.model-name", "test"),
            Map.entry("langchain4j.google-ai-gemini.chat-models.gemini.base-url", server.url("gemini")),
            Map.entry("langchain4j.google-ai.api-key", "test"),
            Map.entry("langchain4j.google-ai.embedding-models.gemini.model-name", "test"),
            Map.entry("langchain4j.google-ai.embedding-models.gemini.base-url", server.url("gemini")),
            Map.entry("langchain4j.open-ai.streaming-chat-models.openai.model-name", "test"),
            Map.entry("langchain4j.mistral-ai.streaming-chat-models.mistral.model-name", "test"),
            Map.entry("langchain4j.ollama.streaming-chat-models.ollama.model-name", "test"),
            Map.entry("langchain4j.google-ai-gemini.streaming-chat-models.gemini.model-name", "test"),
            Map.entry("langchain4j.google-ai-gemini.streaming-chat-models.gemini.base-url", server.url("gemini")),
            Map.entry("langchain4j.bedrock.region", "us-east-1"),
            Map.entry("langchain4j.bedrock.chat-models.bedrock.model-id", "anthropic.claude-3-haiku-20240307-v1:0")
        ));
    }

    @AfterAll
    static void stop() {
        if (context != null) {
            context.close();
        }
        if (server != null) {
            server.close();
        }
    }

    @Test
    void openAi() {
        assertChat("openai");
        assertEmbedding("openai");
    }

    @Test
    void mistralAi() {
        assertChat("mistral");
        assertEmbedding("mistral");
    }

    @Test
    void anthropic() {
        assertChat("anthropic");
    }

    @Test
    void ollama() {
        assertChat("ollama");
        assertEmbedding("ollama");
    }

    @Test
    void googleAiGemini() {
        assertChat("gemini");
        assertEmbedding("gemini");
    }

    @Test
    void streaming() throws InterruptedException {
        for (String name : new String[] {"openai", "mistral", "ollama", "gemini"}) {
            StreamingChatModel model = only(context.getBeansOfType(StreamingChatModel.class, Qualifiers.byName(name)));
            StringBuilder partials = new StringBuilder();
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch complete = new CountDownLatch(1);
            model.chat("Which framework?", new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    partials.append(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    complete.countDown();
                }

                @Override
                public void onError(Throwable throwable) {
                    error.set(throwable);
                    complete.countDown();
                }
            });
            assertTrue(complete.await(10, TimeUnit.SECONDS), () -> name + " partials: " + partials);
            assertNull(error.get(), () -> name + ": " + error.get());
            assertEquals("micronaut", partials.toString(), name);
        }
    }

    @Test
    void toolCallingThroughAnAiService() {
        assertEquals("tool:Micronaut 5", context.getBean(OllamaToolAssistant.class).chat("Which framework?"));
    }

    @Test
    void bedrock() {
        // the AWS SDK client is created, no request is sent
        only(context.getBeansOfType(ChatModel.class, Qualifiers.byName("bedrock")));
    }

    private static void assertChat(String name) {
        ChatModel model = only(context.getBeansOfType(ChatModel.class, Qualifiers.byName(name)));
        assertEquals("micronaut", model.chat("Which framework?"), name);
    }

    private static void assertEmbedding(String name) {
        EmbeddingModel model = only(context.getBeansOfType(EmbeddingModel.class, Qualifiers.byName(name)));
        assertArrayEquals(new float[] {0.1f, 0.2f, 0.3f}, model.embed("Micronaut").content().vector(), name);
    }

    private static <T> T only(Collection<T> beans) {
        assertEquals(1, beans.size(), () -> "beans: " + beans);
        return beans.iterator().next();
    }
}
