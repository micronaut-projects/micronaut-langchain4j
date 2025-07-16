package example.micronaut.aiservice

import dev.langchain4j.model.chat.ChatModel
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AiServiceTest : OllamaTestPropertyProvider {
    @Test
    fun testAiService(friend: Friend, languageModel: ChatModel) {
        val result: String = friend.chat("Hello")
        Assertions.assertNotNull(result)
        Assertions.assertNotNull(languageModel)
    }
}
