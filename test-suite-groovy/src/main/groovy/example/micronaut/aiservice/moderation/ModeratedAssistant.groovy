package example.micronaut.aiservice.moderation

import dev.langchain4j.service.Moderate
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface ModeratedAssistant {

    @Moderate // <1>
    String chat(String userMessage)
}
