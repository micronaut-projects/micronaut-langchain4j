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
@Requires(env = ["agentic-test"])
internal class AgenticTestChatModel : ChatModel {

    override fun chat(userMessage: String): String = RESPONSE

    override fun chat(vararg messages: ChatMessage): ChatResponse = response()

    override fun chat(messages: List<ChatMessage>): ChatResponse = response()

    override fun chat(chatRequest: ChatRequest): ChatResponse = response()

    companion object {
        private const val RESPONSE = "agentic test response"

        private fun response(): ChatResponse = ChatResponse.builder()
            .aiMessage(AiMessage.from(RESPONSE))
            .build()
    }
}
