package example.micronaut

import io.micronaut.context.exceptions.ConfigurationException
import org.jspecify.annotations.NonNull
import io.micronaut.langchain4j.testutils.OllamaUtils
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Map
import java.util.Locale

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class MusicianAssistantTest : TestPropertyProvider {
    @Test
    fun shouldGenerateMusicianTopThreeAlbums(assistant: MusicianAssistant) {
        val musician = assistant.generateTopThreeAlbums("Miles Davis")
        Assertions.assertTrue(musician.albums.lowercase(Locale.getDefault()).contains("kind of blue"))
    }

    override fun getProperties(): @NonNull MutableMap<String, String>? {
        try {
            return Map.of("langchain4j.ollama.base-url", OllamaUtils.ollamaContainerBaseUrl())
        } catch (e: Exception) {
            throw ConfigurationException("Could not set Ollama base URL", e)
        }
    }
}
