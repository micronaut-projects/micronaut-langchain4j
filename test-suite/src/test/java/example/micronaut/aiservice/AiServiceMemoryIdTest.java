package example.micronaut.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "AiServiceMemoryIdTest")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiServiceMemoryIdTest implements OllamaTestPropertyProvider {
    @Test
    void testAiServiceWithMemoryId(Assistant assistant) {
        String sergioId = UUID.randomUUID().toString();
        String timId = UUID.randomUUID().toString();
        assistant.chat(sergioId, "My name is Sergio");
        assistant.chat(timId, "My name is Tim");
        String question = "What is my name?";
        String sergioResponse = assistant.chat(sergioId, question).toLowerCase(Locale.ROOT);
        assertTrue(sergioResponse.contains("sergio"));
        assertFalse(sergioResponse.contains("tim"));
        String timResponse = assistant.chat(timId, question).toLowerCase(Locale.ROOT);
        assertTrue(timResponse.contains("tim"));
        assertFalse(timResponse.contains("sergio"));
    }

    @Requires(property = "spec.name", value = "AiServiceMemoryIdTest")
    @AiService
    interface Assistant {
        String chat(@MemoryId String memoryId, @UserMessage String message);
    }
}
