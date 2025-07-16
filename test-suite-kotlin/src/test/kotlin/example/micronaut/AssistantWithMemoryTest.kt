package example.micronaut

import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.core.annotation.NonNull
import io.micronaut.langchain4j.testutils.OllamaUtils
import io.micronaut.test.support.TestPropertyProvider
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class AssistantWithMemoryTest : TestPropertyProvider {
    override fun getProperties(): @NonNull MutableMap<String, String> {
        val result: MutableMap<String, String> = HashMap<String, String>()
        try {
            result.put("langchain4j.ollama.base-url", OllamaUtils.ollamaContainerBaseUrl())
        } catch (e: Exception) {
            throw ConfigurationException("Could not set Ollama base URL", e)
        }
        return result
    }

    //tag::test[]
    @Test
    fun chatWithMemory(assistant: AssistantWithMemory) {
        val johnConversation = assistant.chat("Let me introduce myself. My name is John")
        val johnConversationId = johnConversation.memoryId
        assertNotNull(johnConversationId)
        val aegonConversation = assistant.chat("Let me introduce myself. My name is Dan")
        val aegonConversationId = aegonConversation.memoryId
        assertNotNull(aegonConversationId)
        var answer = assistant.chat(johnConversationId, "What's my name?")
        assertTrue(answer.response.lowercase().contains("john"), answer.response)
        answer = assistant.chat(aegonConversationId, "What's my name?")
        assertTrue(answer.response.lowercase().contains("dan"), answer.response)
    }
    //end::test[]
}
