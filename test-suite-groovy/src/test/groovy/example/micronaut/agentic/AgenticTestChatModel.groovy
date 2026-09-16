package example.micronaut.agentic

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Singleton
@Primary
@Requires(env = "agentic-test")
class AgenticTestChatModel implements ChatModel {

    private static final String RESPONSE = "agentic test response"

    @Override
    String chat(String userMessage) {
        RESPONSE
    }

    @Override
    ChatResponse chat(ChatMessage... messages) {
        response()
    }

    @Override
    ChatResponse chat(List<ChatMessage> messages) {
        response()
    }

    @Override
    ChatResponse chat(ChatRequest chatRequest) {
        response()
    }

    private static ChatResponse response() {
        ChatResponse.builder()
            .aiMessage(AiMessage.from(RESPONSE))
            .build()
    }
}
