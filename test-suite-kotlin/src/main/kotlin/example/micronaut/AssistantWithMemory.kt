package example.micronaut

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import jakarta.inject.Singleton
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Singleton
class AssistantWithMemory(
    val messageWindowChatMemoryBuilder: MessageWindowChatMemory.Builder,
    val model: ChatModel) {
    private val conversations: MutableMap<String, ChatMemory> = ConcurrentHashMap<String, ChatMemory>()

    fun chat(conversationId: String, message: String): MemoryIdAndResponse {
        val chatMemory = requireNotNull(this.conversations[conversationId]) {
            "Unknown conversation: $conversationId"
        }
        chatMemory.add(UserMessage.from(message))
        val chatResponse = model.chat(chatMemory.messages())
        val aiMessage = chatResponse.aiMessage()
        chatMemory.add(aiMessage)
        return MemoryIdAndResponse(conversationId, aiMessage.text())
    }

    fun chat(message: String): MemoryIdAndResponse {
        val conversationId = startConversation()
        return chat(conversationId, message)
    }

    private fun startConversation(): String {
        val memoryId = generateChatMemoryId()
        val chatMemory = generateChatMemory(memoryId)
        conversations.putIfAbsent(memoryId, chatMemory)
        return memoryId
    }

    private fun generateChatMemoryId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateChatMemory(memoryId: String): ChatMemory {
        return messageWindowChatMemoryBuilder
            .id(memoryId)
            .build()
    }
}
