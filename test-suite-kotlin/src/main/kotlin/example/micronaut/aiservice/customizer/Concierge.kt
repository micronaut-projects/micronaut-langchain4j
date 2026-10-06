package example.micronaut.aiservice.customizer

import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface Concierge {
    @SystemMessage("You are the concierge of a hotel.")
    fun ask(question: String): String
}
