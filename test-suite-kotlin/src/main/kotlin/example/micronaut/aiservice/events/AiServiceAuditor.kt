package example.micronaut.aiservice.events

import dev.langchain4j.observability.api.event.AiServiceCompletedEvent
import dev.langchain4j.observability.api.event.ToolExecutedEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
open class AiServiceAuditor {

    @EventListener // <1>
    open fun onCompleted(event: AiServiceCompletedEvent) {
        LOG.info("AI service {} completed invocation {}",
            event.invocationContext().interfaceName(),
            event.invocationContext().invocationId()) // <2>
    }

    @EventListener
    open fun onToolExecuted(event: ToolExecutedEvent) { // <3>
        LOG.info("Tool {} returned {}", event.request().name(), event.resultText())
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AiServiceAuditor::class.java)
    }
}
