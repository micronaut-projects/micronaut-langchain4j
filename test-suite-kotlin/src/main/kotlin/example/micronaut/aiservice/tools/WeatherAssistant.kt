package example.micronaut.aiservice.tools

import io.micronaut.langchain4j.annotation.AiService

@AiService(toolProviders = ["weather"]) // <1>
interface WeatherAssistant {
    fun ask(question: String): String
}
