package io.micronaut.langchain4j.vertexai.gemini;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
    
    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        VertexAiGeminiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
