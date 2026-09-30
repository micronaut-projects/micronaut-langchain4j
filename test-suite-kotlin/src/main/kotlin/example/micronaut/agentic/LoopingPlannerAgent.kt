package example.micronaut.agentic

import dev.langchain4j.agentic.Agent
import dev.langchain4j.agentic.declarative.LoopAgent
import dev.langchain4j.agentic.workflow.LoopAgentService
import dev.langchain4j.service.UserMessage
import dev.langchain4j.service.V
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.langchain4j.agentic.annotation.AgenticService
import jakarta.inject.Singleton

/**
 * Demonstrates declarative LoopAgent orchestration using Micronaut DI.
 */
@AgenticService(outputKey = "translation")
interface LoopingPlannerAgent {

    @LoopAgent(
        subAgents = [
            TranslatorAgent::class
        ],
        outputKey = "text",
        maxIterations = 3
    )
    fun translatesInLoop(@V("text") text: String): String

    interface TranslatorAgent {
        @UserMessage("""
            You are a translator.
            If the text is in English, translate to French.
            If the text is in French, translate to German.
            If the text is in German, translate to Spanish.
            Translate this: "{{text}}". Answer with the translation only, no explanations, no details.
            """)
        @Agent(outputKey = "text")
        fun translate(@V("text") text: String): String
    }

    @Singleton
    @Requires(property = "spec.name", value = "LoopingPlannerAgentTest")
    class LoopBuilderListener : BeanCreatedEventListener<LoopAgentService<*>> {

        override fun onCreated(event: BeanCreatedEvent<LoopAgentService<*>>): LoopAgentService<*> {
            val builder = event.bean
            builder.exitCondition { _, idx -> idx == 3 }
            return builder
        }
    }
}
