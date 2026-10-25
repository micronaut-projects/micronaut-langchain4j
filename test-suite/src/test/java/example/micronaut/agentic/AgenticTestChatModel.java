package example.micronaut.agentic;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
@Primary
@Requires(env = "agentic-test")
final class AgenticTestChatModel implements ChatModel {

    private static final String RESPONSE = "agentic test response";

    @Override
    public String chat(String userMessage) {
        return RESPONSE;
    }

    @Override
    public ChatResponse chat(ChatMessage... messages) {
        return response();
    }

    @Override
    public ChatResponse chat(List<ChatMessage> messages) {
        return response();
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        return response();
    }

    private static ChatResponse response() {
        return ChatResponse.builder()
            .aiMessage(AiMessage.from(RESPONSE))
            .build();
    }
}
