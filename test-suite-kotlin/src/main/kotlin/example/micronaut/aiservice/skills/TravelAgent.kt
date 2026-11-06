package example.micronaut.aiservice.skills

import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService

@AiService(skills = "travel") // <1>
interface TravelAgent {

    @SystemMessage("You are a travel agent") // <2>
    fun chat(message: String): String
}
