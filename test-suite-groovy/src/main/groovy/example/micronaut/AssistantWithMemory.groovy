package example.micronaut

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import io.micronaut.langchain4j.store.memory.chat.MessageWindowChatMemoryFactory
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class AssistantWithMemory {
    private final Map<String, ChatMemory> conversations = new ConcurrentHashMap<>()
    private final MessageWindowChatMemoryFactory messageWindowChatMemoryFactory
    private final ChatModel model
    private final ChatMemoryStore chatMemoryStore

    AssistantWithMemory(MessageWindowChatMemoryFactory messageWindowChatMemoryFactory,
                               ChatMemoryStore chatMemoryStore,
                               ChatModel model) {
        this.messageWindowChatMemoryFactory = messageWindowChatMemoryFactory
        this.model = model
        this.chatMemoryStore = chatMemoryStore
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
        messageWindowChatMemoryFactory.withChatMemoryStore(chatMemoryStore)
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
