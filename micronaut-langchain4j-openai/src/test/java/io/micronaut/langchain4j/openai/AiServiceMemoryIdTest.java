package io.micronaut.langchain4j.openai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "AiServiceMemoryIdTest")
@MicronautTest(startApplication = false)
class AiServiceMemoryIdTest {

    @Test
    void testAiServiceWithMemoryId(Assistant assistant) {
        String sergioId = UUID.randomUUID().toString();
        String timId = UUID.randomUUID().toString();
        assistant.chat(sergioId, "My name is Sergio");
        assistant.chat(timId, "My name is Tim");
        String sergioResponse = assistant.chat(sergioId, "Do you remember my name?").toLowerCase(Locale.ROOT);
        assertTrue(sergioResponse.contains("sergio"));
        assertFalse(sergioResponse.contains("tim"));
        String timResponse = assistant.chat(timId, "Do you remember my name?").toLowerCase(Locale.ROOT);
        assertTrue(timResponse.contains("tim"));
        assertFalse(timResponse.contains("sergio"));
    }

    @Requires(property = "spec.name", value = "AiServiceMemoryIdTest")
    @AiService
    interface Assistant {
        String chat(@MemoryId String memoryId, @UserMessage String message);
    }
}
