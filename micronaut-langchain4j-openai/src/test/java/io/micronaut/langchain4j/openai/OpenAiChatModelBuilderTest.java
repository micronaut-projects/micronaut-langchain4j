package io.micronaut.langchain4j.openai;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.LoadBalancer;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAiChatModelBuilderTest {
    private static URI serverUri;
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void openAiChatModelBuilder() throws IOException {
        startOpenAiServer();

        try (ApplicationContext context = context()) {
            assertTrue(context.containsBean(ChatModel.class));
            assertTrue(context.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class));
        }
    }

    @Test
    void openAiChatModelUsesInjectedHttpClientBuilder() throws IOException {
        startOpenAiServer();

        try (ApplicationContext context = context()) {
            assertEquals("micronaut", context.getBean(ChatModel.class).chat("Which client is active?"));
        }
    }

    @Test
    void openAiStreamingChatModelUsesInjectedHttpClientBuilder() throws IOException, InterruptedException {
        startOpenAiServer();

        try (ApplicationContext context = context()) {
            StringBuilder partials = new StringBuilder();
            AtomicReference<Throwable> error = new AtomicReference<>();
            CountDownLatch complete = new CountDownLatch(1);

            context.getBean(StreamingChatModel.class).chat("Which client is active?", new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponsePart) {
                    partials.append(partialResponsePart);
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

            assertTrue(complete.await(5, TimeUnit.SECONDS));
            assertNull(error.get());
            assertEquals("micronaut", partials.toString());
        }
    }

    private ApplicationContext context() {
        return ApplicationContext.run(Map.of(
            "langchain4j.open-ai.api-key", "blah",
            "langchain4j.open-ai.organization-id", "blah",
            "langchain4j.open-ai.base-url", url("/")
        ));
    }

    private void startOpenAiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            assertEquals("filtered", exchange.getRequestHeaders().getFirst("X-Micronaut-Client"));
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String accept = exchange.getRequestHeaders().getFirst("Accept");
            boolean stream = accept != null && accept.contains("text/event-stream") ||
                requestBody.contains("\"stream\"") && requestBody.contains("true");
            byte[] response = stream ? streamingResponse() : chatResponse();
            exchange.getResponseHeaders().add(
                "Content-Type",
                stream ? "text/event-stream" : "application/json"
            );
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        serverUri = URI.create(url("/"));
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static byte[] chatResponse() {
        return """
            {
              "id": "chatcmpl-test",
              "object": "chat.completion",
              "created": 0,
              "model": "gpt-3.5-turbo",
              "choices": [
                {
                  "index": 0,
                  "message": {
                    "role": "assistant",
                    "content": "micronaut"
                  },
                  "finish_reason": "stop"
                }
              ],
              "usage": {
                "prompt_tokens": 1,
                "completion_tokens": 1,
                "total_tokens": 2
              }
            }
            """.getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] streamingResponse() {
        return """
            event: message
            data: {"id":"chatcmpl-test","object":"chat.completion.chunk","created":0,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"role":"assistant","content":"micro"},"finish_reason":null}]}

            event: message
            data: {"id":"chatcmpl-test","object":"chat.completion.chunk","created":0,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":"naut"},"finish_reason":"stop"}],"usage":{"prompt_tokens":1,"completion_tokens":1,"total_tokens":2}}

            event: message
            data: [DONE]

            """.getBytes(StandardCharsets.UTF_8);
    }

    @Filter("/chat/completions")
    static class TestClientFilter implements HttpClientFilter {
        @Override
        public Publisher<? extends io.micronaut.http.HttpResponse<?>> doFilter(
            MutableHttpRequest<?> request,
            ClientFilterChain chain) {
            request.header("X-Micronaut-Client", "filtered");
            return chain.proceed(request);
        }
    }

    @Factory
    static class TestHttpClientFactory {
        @Bean
        @Primary
        @Singleton
        io.micronaut.http.client.HttpClient httpClient(
            BeanContext beanContext,
            HttpClientRegistry<io.micronaut.http.client.HttpClient> httpClientRegistry) {
            return httpClientRegistry.resolveClient(null, LoadBalancer.fixed(serverUri), null, beanContext);
        }
    }
}
