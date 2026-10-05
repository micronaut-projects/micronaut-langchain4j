package io.micronaut.langchain4j.micrometer;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.micrometer.metrics.listeners.MicrometerMetricsChatModelListener;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.observation.listener.ObservationChatModelListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.observation.ObservationRegistry;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatModelMetricsTest {

    private HttpServer server;

    @BeforeEach
    void startOpenAiServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/chat/completions", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = """
                {"id":"chatcmpl-test","object":"chat.completion","created":0,"model":"gpt-4.1-mini",
                 "choices":[{"index":0,"message":{"role":"assistant","content":"micronaut"},"finish_reason":"stop"}],
                 "usage":{"prompt_tokens":3,"completion_tokens":1,"total_tokens":4}}
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void recordsTheGenAiMetricsOfTheChatModels() {
        try (ApplicationContext context = context(Map.of())) {
            assertInstanceOf(MicrometerMetricsChatModelListener.class, context.getBean(ChatModelListener.class));
            assertEquals("micronaut", context.getBean(ChatModel.class).chat("Hello"));

            MeterRegistry registry = context.getBean(MeterRegistry.class);
            double tokens = registry.find("gen_ai.client.token.usage").summaries().stream()
                .mapToDouble(summary -> summary.totalAmount())
                .sum();
            assertEquals(4, tokens);
        }
    }

    @Test
    void usesTheObservationListenerWhenThereIsAnObservationRegistry() {
        try (ApplicationContext context = context(Map.of("spec.observation", true))) {
            assertInstanceOf(ObservationChatModelListener.class, context.getBean(ChatModelListener.class));
            assertEquals("micronaut", context.getBean(ChatModel.class).chat("Hello"));
        }
    }

    @Test
    void canBeDisabled() {
        try (ApplicationContext context = context(Map.of("langchain4j.micrometer.enabled", false))) {
            assertFalse(context.containsBean(ChatModelListener.class));
            assertEquals("micronaut", context.getBean(ChatModel.class).chat("Hello"));
        }
    }

    private ApplicationContext context(Map<String, Object> properties) {
        Map<String, Object> all = new HashMap<>(properties);
        all.put("langchain4j.open-ai.api-key", "test");
        all.put("langchain4j.open-ai.base-url", "http://localhost:" + server.getAddress().getPort() + "/");
        all.put("spec.name", "ChatModelMetricsTest");
        return ApplicationContext.run(all);
    }

    @Factory
    @Requires(property = "spec.name", value = "ChatModelMetricsTest")
    static class RegistryFactory {
        @Singleton
        MeterRegistry meterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Singleton
        @Requires(property = "spec.observation", value = "true")
        ObservationRegistry observationRegistry() {
            return ObservationRegistry.create();
        }
    }
}
