package io.micronaut.langchain4j.vertexai.gemini;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

@MicronautTest
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
        GoogleCredentials builderCredentials = credentials(chatLanguageModelConfiguration.getBuilder());
        assertNotNull(builderCredentials);
        assertNotNull(builderCredentials.getAccessToken());
        assertNotNull(googleCredentials.getAccessToken());
        org.junit.jupiter.api.Assertions.assertEquals(
            googleCredentials.getAccessToken().getTokenValue(),
            builderCredentials.getAccessToken().getTokenValue()
        );
        VertexAiGeminiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    private static GoogleCredentials credentials(Object builder) {
        for (Class<?> type = builder.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (GoogleCredentials.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        return (GoogleCredentials) field.get(builder);
                    } catch (IllegalAccessException e) {
                        throw new AssertionError(e);
                    }
                }
            }
        }
        throw new AssertionError("No GoogleCredentials field found on builder " + builder.getClass().getName());
    }

    @Factory
    static final class TestFactory {
        @Singleton
        GoogleCredentials googleCredentials() {
            return GoogleCredentials.create(
                new AccessToken("test-token", Date.from(Instant.parse("2030-01-01T00:00:00Z")))
            );
        }
    }
}
