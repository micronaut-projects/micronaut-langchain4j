package example.micronaut;

import io.micronaut.context.exceptions.ConfigurationException;
import org.jspecify.annotations.NonNull;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Testcontainers;
import io.micronaut.langchain4j.testutils.OllamaUtils;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MusicianAssistantTest implements TestPropertyProvider {
    @Test
    void shouldGenerateMusicianTopThreeAlbums(MusicianAssistant assistant) {
        Musician musician = assistant.generateTopThreeAlbums("Miles Davis");
        assertTrue(musician.albums().toLowerCase().contains("kind of blue"));
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
