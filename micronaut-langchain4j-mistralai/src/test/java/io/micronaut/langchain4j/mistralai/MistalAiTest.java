package io.micronaut.langchain4j.mistralai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.mistralai.MistralAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.mistral-ai.chat-models.orca-mini.api-key", value = "blah")
public class MistalAiTest {

    @Inject
    MistralAiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        MistralAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
