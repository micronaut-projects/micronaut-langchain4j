package example.micronaut.aiservice.guardrails

import dev.langchain4j.service.guardrail.InputGuardrails
import io.micronaut.langchain4j.annotation.AiService

@AiService
interface GuardedAssistant {

    @InputGuardrails(PromptInjectionGuard::class) // <1>
    fun chat(userMessage: String): String
}
