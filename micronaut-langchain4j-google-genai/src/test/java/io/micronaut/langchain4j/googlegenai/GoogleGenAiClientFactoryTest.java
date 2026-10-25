package io.micronaut.langchain4j.googlegenai;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Google Gen AI client is configured with {@code langchain4j.google-gen-ai}.
 */
class GoogleGenAiClientFactoryTest {

    @Test
    void configuresTheGeminiApiClient() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.google-gen-ai.api-key", "test-key",
            "langchain4j.google-gen-ai.base-url", "https://gemini.example.com/",
            "langchain4j.google-gen-ai.api-version", "v1",
            "langchain4j.google-gen-ai.timeout", "5s",
            "langchain4j.google-gen-ai.max-connections", "7",
            "langchain4j.google-gen-ai.max-connections-per-host", "3"))) {
            HttpOptions httpOptions = context.getBean(HttpOptions.Builder.class).build();
            assertEquals(Optional.of("https://gemini.example.com/"), httpOptions.baseUrl());
            assertEquals(Optional.of("v1"), httpOptions.apiVersion());
            assertEquals(Optional.of(5000), httpOptions.timeout());

            ClientOptions clientOptions = context.getBean(ClientOptions.Builder.class).build();
            assertEquals(Optional.of(7), clientOptions.maxConnections());
            assertEquals(Optional.of(3), clientOptions.maxConnectionsPerHost());

            Client client = context.getBean(Client.class);
            assertFalse(client.vertexAI());
            assertEquals("test-key", client.apiKey());
        }
    }

    @Test
    void configuresTheVertexAiClient() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", "GoogleGenAiClientFactoryTest",
            "langchain4j.google-gen-ai.vertex-ai", "true",
            "langchain4j.google-gen-ai.project-id", "my-project",
            "langchain4j.google-gen-ai.location", "europe-west1"))) {
            Client client = context.getBean(Client.class);
            assertTrue(client.vertexAI());
            assertEquals("my-project", client.project());
            assertEquals("europe-west1", client.location());
        }
    }

    @Factory
    @Requires(property = "spec.name", value = "GoogleGenAiClientFactoryTest")
    static class Credentials {
        @Singleton
        GoogleCredentials credentials() {
            return GoogleCredentials.create(new AccessToken("token", null));
        }
    }
}
