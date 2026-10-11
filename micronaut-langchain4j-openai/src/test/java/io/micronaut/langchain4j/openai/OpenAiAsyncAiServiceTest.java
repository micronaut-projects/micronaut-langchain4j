package io.micronaut.langchain4j.openai;

import com.sun.net.httpserver.HttpServer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.FlowAdapters;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Asynchronous and reactive AI services with the OpenAI models, whose non-blocking API (LangChain4j's
 * {@code doChatAsync} and {@code Flow.Publisher} streaming) runs on the Micronaut HTTP client.
 */
class OpenAiAsyncAiServiceTest {

    private static final String SPEC_NAME = "OpenAiAsyncAiServiceTest";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private HttpServer server;
    private ApplicationContext context;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\s", "");
            boolean stream = request.contains("\"stream\":true");
            byte[] response = (stream ? """
                data: {"id":"c","object":"chat.completion.chunk","created":0,"model":"test","choices":[{"index":0,"delta":{"role":"assistant","content":"micro"},"finish_reason":null}]}

                data: {"id":"c","object":"chat.completion.chunk","created":0,"model":"test","choices":[{"index":0,"delta":{"content":"naut"},"finish_reason":"stop"}]}

                data: [DONE]

                """ : """
                {"id":"c","object":"chat.completion","created":0,"model":"test",
                 "choices":[{"index":0,"message":{"role":"assistant","content":"micronaut"},"finish_reason":"stop"}],
                 "usage":{"prompt_tokens":3,"completion_tokens":1,"total_tokens":4}}
                """).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", stream ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.open-ai.api-key", "test",
            "langchain4j.open-ai.base-url", "http://localhost:" + server.getAddress().getPort() + "/",
            "langchain4j.open-ai.chat-model.model-name", "test",
            "langchain4j.open-ai.streaming-chat-model.model-name", "test"
        ));
    }

    @AfterEach
    void stop() {
        context.close();
        server.stop(0);
    }

    @Test
    void completableFuture() throws Exception {
        assertEquals("micronaut", context.getBean(AsyncOpenAiAssistant.class).future("Which framework?").get(10, TimeUnit.SECONDS));
    }

    @Test
    void flowPublisher() {
        Flow.Publisher<String> publisher = context.getBean(AsyncOpenAiAssistant.class).flow("Which framework?");
        assertEquals(List.of("micro", "naut"), Flux.from(FlowAdapters.toPublisher(publisher)).collectList().block(TIMEOUT));
    }

    @Test
    void reactiveStreamsPublisher() {
        Publisher<String> publisher = context.getBean(AsyncOpenAiAssistant.class).publisher("Which framework?");
        assertEquals(List.of("micro", "naut"), Flux.from(publisher).collectList().block(TIMEOUT));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface AsyncOpenAiAssistant {
        CompletableFuture<String> future(String userMessage);

        Flow.Publisher<String> flow(String userMessage);

        Publisher<String> publisher(String userMessage);
    }
}
