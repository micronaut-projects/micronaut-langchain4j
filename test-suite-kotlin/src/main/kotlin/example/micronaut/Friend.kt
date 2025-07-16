package example.micronaut

import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface Friend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    fun chat(userMessage: String): String
}
