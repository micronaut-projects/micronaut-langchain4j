package io.micronaut.langchain4j.vertexai.gemini;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "spec.name", value = "VertexAiGeminiTest")
@Property(name = "langchain4j.vertex-ai-gemini.model-name", value = "orca-mini")
@Property(name = "langchain4j.vertex-ai-gemini.endpoint", value = "blah")
@Property(name = "langchain4j.vertex-ai-gemini.project", value = "myproject")
@Property(name = "langchain4j.vertex-ai-gemini.location", value = "somewhere")
@Property(name = "langchain4j.vertex-ai-gemini.publisher", value = "whoever")
public class VertexAiGeminiTest {

    @Inject
    DefaultVertexAiGeminiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    GoogleCredentials googleCredentials;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        assertSame(googleCredentials, chatLanguageModelConfiguration.getCredentials());
        VertexAiGeminiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    @Test
    void usesDefaultCredentialsWhenNoCredentialsBeanIsPresent() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.vertex-ai-gemini.model-name", "orca-mini",
            "langchain4j.vertex-ai-gemini.project", "myproject",
            "langchain4j.vertex-ai-gemini.location", "somewhere"
        ))) {
            assertNull(context.getBean(DefaultVertexAiGeminiChatModelConfiguration.class).getCredentials());
        }
    }

    @Factory
    @Requires(property = "spec.name", value = "VertexAiGeminiTest")
    static final class TestFactory {
        @Singleton
        GoogleCredentials googleCredentials() {
            return GoogleCredentials.create(
                new AccessToken("test-token", Date.from(Instant.parse("2030-01-01T00:00:00Z")))
            );
        }
    }
}
