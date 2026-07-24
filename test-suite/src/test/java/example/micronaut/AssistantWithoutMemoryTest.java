package example.micronaut;

import io.micronaut.context.exceptions.ConfigurationException;
import org.jspecify.annotations.NonNull;
import io.micronaut.langchain4j.testutils.OllamaUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssistantWithoutMemoryTest implements TestPropertyProvider {
    @Test
    void chatWithoutMemory(AssistantWithoutMemory assistant) {
        assertDoesNotThrow(() -> assistant.chat("My Name is Sergio"));
        String response = assistant.chat("What's my name?");
        assertFalse(response.toLowerCase().contains("sergio"), response);
    }

    @Override
    public @NonNull Map<String, String> getProperties() {
        try {
            return Map.of("langchain4j.ollama.base-url", OllamaUtils.ollamaContainerBaseUrl());
        } catch (Exception e) {
            throw new ConfigurationException("Could not set Ollama base URL", e);
        }
    }
}
