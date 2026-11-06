package example.micronaut.aiservice.mcp

import io.micronaut.langchain4j.annotation.AiService

@AiService(mcpClients = ["github"]) // <1>
interface GitHubAssistant {
    fun ask(question: String): String
}
