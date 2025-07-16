package example.micronaut

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import jakarta.inject.Singleton

import java.util.concurrent.ConcurrentHashMap

@Singleton
class AssistantWithMemory {
    private final Map<String, ChatMemory> conversations = new ConcurrentHashMap<>()
    private final MessageWindowChatMemory.Builder messageWindowChatMemoryBuilder
    private final ChatModel model

    AssistantWithMemory(MessageWindowChatMemory.Builder messageWindowChatMemoryBuilder,
                        ChatModel model) {
        this.model = model
        this.messageWindowChatMemoryBuilder = messageWindowChatMemoryBuilder
    }

    private String startConversation() {
        String memoryId = generateChatMemoryId()
        ChatMemory chatMemory = generateChatMemory(memoryId)
        conversations.putIfAbsent(memoryId, chatMemory)
        memoryId
    }

    private String generateChatMemoryId() {
        UUID.randomUUID().toString()
    }

    private ChatMemory generateChatMemory(String memoryId) {
        messageWindowChatMemoryBuilder
                .id(memoryId)
                .build()
    }

    MemoryIdAndResponse chat(String memoryId, String message) {
        ChatMemory chatMemory = conversations.get(memoryId)
        if (chatMemory == null) {
            throw new IllegalArgumentException("Unknown conversation: " + memoryId)
        }
        chatMemory.add(UserMessage.from(message))
        ChatResponse chatResponse = model.chat(chatMemory.messages())
        AiMessage aiMessage = chatResponse.aiMessage()
        chatMemory.add(aiMessage)
        new MemoryIdAndResponse(memoryId: memoryId, response: aiMessage.text())
    }

    MemoryIdAndResponse chat(String message) {
        String conversationId = startConversation()
        chat(conversationId, message)
    }
}
