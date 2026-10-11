package example.micronaut.aiservice.events;

import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AiServiceAuditor {

    private static final Logger LOG = LoggerFactory.getLogger(AiServiceAuditor.class);

    @EventListener // <1>
    void onCompleted(AiServiceCompletedEvent event) {
        LOG.info("AI service {} completed invocation {}",
            event.invocationContext().interfaceName(),
            event.invocationContext().invocationId()); // <2>
    }

    @EventListener
    void onToolExecuted(ToolExecutedEvent event) { // <3>
        LOG.info("Tool {} returned {}", event.request().name(), event.resultText());
    }
}
