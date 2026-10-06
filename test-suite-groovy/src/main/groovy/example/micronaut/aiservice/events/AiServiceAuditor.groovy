package example.micronaut.aiservice.events

import dev.langchain4j.observability.api.event.AiServiceCompletedEvent
import dev.langchain4j.observability.api.event.ToolExecutedEvent
import groovy.util.logging.Slf4j
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton

@Slf4j
@Singleton
class AiServiceAuditor {

    @EventListener // <1>
    void onCompleted(AiServiceCompletedEvent event) {
        log.info("AI service {} completed invocation {}",
            event.invocationContext().interfaceName(),
            event.invocationContext().invocationId()) // <2>
    }

    @EventListener
    void onToolExecuted(ToolExecutedEvent event) { // <3>
        log.info("Tool {} returned {}", event.request().name(), event.resultText())
    }
}
