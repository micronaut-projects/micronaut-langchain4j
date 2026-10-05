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
 * Chat model of the {@code structured-output} environment: it records the request and answers with JSON matching
 * the requested schema.
 */
@Singleton
@Primary
@Requires(env = "structured-output")
class StructuredOutputChatModel implements ChatModel {

    private static final String TALK = '{"title": "Structured outputs", "reason": "You like LLMs", "score": 5}'

    ChatRequest lastRequest

    @Override
    ChatResponse doChat(ChatRequest chatRequest) {
        lastRequest = chatRequest
        String answer = chatRequest.responseFormat().jsonSchema().name().startsWith("List_of_") ? "{\"values\": [${TALK}]}".toString() : TALK
        ChatResponse.builder().aiMessage(AiMessage.from(answer)).build()
    }

    @Override
    Set<Capability> supportedCapabilities() {
        Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
    }
}
