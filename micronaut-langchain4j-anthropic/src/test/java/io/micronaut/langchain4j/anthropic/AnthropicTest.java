package io.micronaut.langchain4j.anthropic;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.anthropic.api-key", value = "blah")
public class AnthropicTest {

    @Inject
    DefaultAnthropicChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        AnthropicChatModel chatModel = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(chatModel);
    }

}
