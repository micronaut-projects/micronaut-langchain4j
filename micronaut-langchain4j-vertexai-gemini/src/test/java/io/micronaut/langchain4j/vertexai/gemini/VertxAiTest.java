package io.micronaut.langchain4j.vertexai.gemini;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.vertex-ai-gemini.chat-models.orca-mini.endpoint", value = "blah")
@Property(name = "langchain4j.vertex-ai-gemini.chat-models.orca-mini.project", value = "myproject")
@Property(name = "langchain4j.vertex-ai-gemini.chat-models.orca-mini.location", value = "somewhere")
@Property(name = "langchain4j.vertex-ai-gemini.chat-models.orca-mini.publisher", value = "whoever")
public class VertxAiTest {

    @Inject
    VertexAiGeminiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        VertexAiGeminiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
