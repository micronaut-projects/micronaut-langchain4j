package io.micronaut.langchain4j.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.open-ai.api-key", value = "blah")
@Property(name = "langchain4j.open-ai.organization-id", value = "blah")
class OpenAiAiTest {

    @Inject
    DefaultOpenAiChatModelConfiguration chatLanguageModelConfiguration;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        OpenAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
