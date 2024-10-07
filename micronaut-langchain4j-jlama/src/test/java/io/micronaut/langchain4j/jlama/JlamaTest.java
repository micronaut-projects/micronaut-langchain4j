package io.micronaut.langchain4j.jlama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.jlama.chat-model.model-name", value = "tjake/TinyLlama-1.1B-Chat-v1.0-Jlama-Q4")
public class JlamaTest {

    @Inject
    ChatLanguageModel chatLanguageModel;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testJlamaLanguageModel() {
        Assertions.assertNotNull(chatLanguageModel);
    }

}
