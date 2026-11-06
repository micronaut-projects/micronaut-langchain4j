package example.micronaut.aiservice.flow

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.annotation.AiService
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@Requires(property = "spec.name", value = "FlowAiServiceTest")
@AiService
interface FlowAssistant {
    fun chat(userMessage: String): Flow<String>
}

@Factory
@Requires(property = "spec.name", value = "FlowAiServiceTest")
class FlowModelFactory {
    @Singleton
    @Primary
    fun streamingChatModel(): StreamingChatModel = object : StreamingChatModel {
        override fun doChat(chatRequest: ChatRequest, handler: StreamingChatResponseHandler) {
            handler.onPartialResponse("micro")
            handler.onPartialResponse("naut")
            handler.onCompleteResponse(ChatResponse.builder().aiMessage(AiMessage.from("micronaut")).build())
        }
    }
}

@Property(name = "langchain4j.ollama.enabled", value = "false")
@Property(name = "spec.name", value = "FlowAiServiceTest")
@MicronautTest(startApplication = false)
class FlowAiServiceTest(private val assistant: FlowAssistant) {

    @Test
    fun streamsAKotlinFlow() = runBlocking {
        assertEquals(listOf("micro", "naut"), assistant.chat("Hello").toList())
    }
}
