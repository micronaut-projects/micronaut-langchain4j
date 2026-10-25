package example.micronaut

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

abstract class AssistantWithMemoryTest : OllamaTestPropertyProvider {
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
