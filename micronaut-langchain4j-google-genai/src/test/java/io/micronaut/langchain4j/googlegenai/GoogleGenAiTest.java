package io.micronaut.langchain4j.googlegenai;

import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Google Gen AI models against a fake Gemini API endpoint.
 */
class GoogleGenAiTest {

    private static final String GENERATE = """
        {"candidates":[{"content":{"role":"model","parts":[{"text":"micronaut"}]},"finishReason":"STOP"}],
         "usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1,"totalTokenCount":4},"modelVersion":"test"}
        """;
    private static final String STREAM = """
        data: {"candidates":[{"content":{"role":"model","parts":[{"text":"micro"}]}}],"modelVersion":"test"}

        data: {"candidates":[{"content":{"role":"model","parts":[{"text":"naut"}]},"finishReason":"STOP"}],"usageMetadata":{"promptTokenCount":3,"candidatesTokenCount":1,"totalTokenCount":4},"modelVersion":"test"}

        """;
    private static final String EMBED = """
        {"embeddings":[{"values":[0.1,0.2]}]}
        """;

    private final List<String> paths = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ApplicationContext context;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path = exchange.getRequestURI().getPath();
            paths.add(path + "?" + exchange.getRequestURI().getQuery());
            boolean stream = path.endsWith(":streamGenerateContent");
            String body = stream ? STREAM : path.contains("mbed") ? EMBED : GENERATE;
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", stream ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        context = ApplicationContext.run(Map.of(
            "spec.name", "GoogleGenAiTest",
            "langchain4j.google-gen-ai.api-key", "test",
            "langchain4j.google-gen-ai.base-url", "http://localhost:" + server.getAddress().getPort() + "/",
            "langchain4j.google-gen-ai.max-connections", "7",
            "langchain4j.google-gen-ai.chat-model.model-name", "test",
            "langchain4j.google-gen-ai.streaming-chat-model.model-name", "test",
            "langchain4j.google-gen-ai.embedding-model.model-name", "test"));
    }

    @AfterEach
    void stop() {
        context.close();
        server.stop(0);
    }

    @Test
    void chat() {
        assertEquals("micronaut", context.getBean(ChatModel.class).chat("Which framework?"));
    }

    @Test
    void streamingChat() throws InterruptedException {
        StringBuilder partials = new StringBuilder();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch complete = new CountDownLatch(1);
        context.getBean(StreamingChatModel.class).chat("Which framework?", new StreamingChatResponseHandler() {
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
        assertTrue(complete.await(10, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals("micronaut", partials.toString());
    }

    @Test
    void embedding() {
        assertArrayEquals(new float[] {0.1f, 0.2f}, context.getBean(EmbeddingModel.class).embed("Micronaut").content().vector());
    }

    @Test
    void theModelsShareTheConfiguredClient() {
        assertSame(context.getBean(Client.class), context.getBean(Client.class));
        assertEquals(7, context.getBean(ClientOptionsRecorder.class).maxConnections.get());
    }

    @Singleton
    @Requires(property = "spec.name", value = "GoogleGenAiTest")
    static class ClientOptionsRecorder implements BeanCreatedEventListener<ClientOptions.Builder> {
        final AtomicReference<Integer> maxConnections = new AtomicReference<>();

        @Override
        public ClientOptions.Builder onCreated(BeanCreatedEvent<ClientOptions.Builder> event) {
            maxConnections.set(event.getBean().build().maxConnections().orElse(null));
            return event.getBean();
        }
    }
}
