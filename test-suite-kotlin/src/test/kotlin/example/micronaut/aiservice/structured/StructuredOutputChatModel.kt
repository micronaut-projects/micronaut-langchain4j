package example.micronaut.aiservice.structured

import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.ChatResponse
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

/**
 * Chat model of the `structured-output` environment: it records the request and answers with JSON matching
 * the requested schema.
 */
@Singleton
@Primary
@Requires(env = ["structured-output"])
class StructuredOutputChatModel : ChatModel {

    var lastRequest: ChatRequest? = null
        private set

    override fun doChat(chatRequest: ChatRequest): ChatResponse {
        lastRequest = chatRequest
        val answer = if (chatRequest.responseFormat().jsonSchema().name().startsWith("List_of_")) "{\"values\": [$TALK]}" else TALK
        return ChatResponse.builder().aiMessage(AiMessage.from(answer)).build()
    }

    override fun supportedCapabilities(): Set<Capability> = setOf(Capability.RESPONSE_FORMAT_JSON_SCHEMA)

    companion object {
        private const val TALK = """{"title": "Structured outputs", "reason": "You like LLMs", "score": 5}"""
    }
}
