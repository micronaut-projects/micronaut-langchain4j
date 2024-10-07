package io.micronaut.langchain4j.huggingface;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.hugging-face.access-token", value = "blah")
public class HuggingFaceTest {
    @Inject
    DefaultHuggingFaceChatModelConfiguration chatLanguageModelConfiguration;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        HuggingFaceChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }
}
