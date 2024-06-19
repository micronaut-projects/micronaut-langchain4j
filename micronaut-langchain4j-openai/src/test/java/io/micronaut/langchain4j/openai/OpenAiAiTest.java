package io.micronaut.langchain4j.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.open-ai.chat-models.orca-mini.api-key", value = "blah")
public class OpenAiAiTest {

    @Inject
    OpenAiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        OpenAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
