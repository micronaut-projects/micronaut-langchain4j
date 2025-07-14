package example.micronaut;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class AssistantWithoutMemory {
    private static final SystemMessage SYSTEM_MSG = SystemMessage.from("""
      You are a Vintage store chat bot.""");

    private final ChatModel model;

    public AssistantWithoutMemory(ChatModel model) {
        this.model = model;
    }

    public String chat(String message) {
        ChatResponse response = model.chat(List.of(SYSTEM_MSG, UserMessage.from(message)));
        return response.aiMessage().text();
    }
}
