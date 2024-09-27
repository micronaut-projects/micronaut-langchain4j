package io.micronaut.langchain4j.ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.SystemMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "langchain4j.ollama.model-name", value = "orca-mini")
public class OllamaTest {

    @Inject
    ChatLanguageModel chatLanguageModel;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testOllamaLanguageModel() {
        Assertions.assertNotNull(chatLanguageModel);
    }

    @Test
    @Disabled("404 error: Something broken in Ollama Testcontainers?")
    void testAiService(Friend friend) {
        String result = friend.chat("Hello");

        Assertions.assertNotNull(result);
    }

    @AiService
    interface Friend {

        @SystemMessage("You are a good friend of mine. Answer using slang.")
        String chat(String userMessage);
    }

}
