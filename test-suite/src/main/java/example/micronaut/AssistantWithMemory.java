package example.micronaut;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AssistantWithMemory {
    private final Map<String, ChatMemory> conversations = new ConcurrentHashMap<>();
    private final ChatModel model;
    private final MessageWindowChatMemory.Builder messageWindowChatMemoryBuilder;

    public AssistantWithMemory(MessageWindowChatMemory.Builder messageWindowChatMemoryBuilder,
                               ChatModel model) {
        this.messageWindowChatMemoryBuilder = messageWindowChatMemoryBuilder;
        this.model = model;
    }

    public MemoryIdAndResponse chat(String conversationId, String message) {
        ChatMemory chatMemory = conversations.get(conversationId);
        if (chatMemory == null) {
            throw new IllegalArgumentException("Unknown conversation: " + conversationId);
        }
        chatMemory.add(UserMessage.from(message));
        ChatResponse chatResponse = model.chat(chatMemory.messages());
        AiMessage answer = chatResponse.aiMessage();
        chatMemory.add(answer);
        return new MemoryIdAndResponse(conversationId, answer.text());
    }

    public MemoryIdAndResponse chat(String message) {
        String conversationId = startConversation();
        return chat(conversationId, message);
    }

    private String startConversation() {
        String memoryId = generateChatMemoryId();
        ChatMemory chatMemory = generateChatMemory(memoryId);
        conversations.putIfAbsent(memoryId, chatMemory);
        return memoryId;
    }

    private String generateChatMemoryId() {
        return UUID.randomUUID().toString();
    }

    private ChatMemory generateChatMemory(String memoryId) {
        return messageWindowChatMemoryBuilder
            .id(memoryId)
            .build();
    }
}
