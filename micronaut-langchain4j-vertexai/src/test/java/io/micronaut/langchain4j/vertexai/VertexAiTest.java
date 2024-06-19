package io.micronaut.langchain4j.vertexai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.vertexai.VertexAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.vertex-ai.chat-models.orca-mini.endpoint", value = "blah")
@Property(name = "langchain4j.vertex-ai.chat-models.orca-mini.project", value = "myproject")
@Property(name = "langchain4j.vertex-ai.chat-models.orca-mini.location", value = "somewhere")
@Property(name = "langchain4j.vertex-ai.chat-models.orca-mini.publisher", value = "whoever")
public class VertexAiTest {

    @Inject
    VertexAiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        VertexAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
