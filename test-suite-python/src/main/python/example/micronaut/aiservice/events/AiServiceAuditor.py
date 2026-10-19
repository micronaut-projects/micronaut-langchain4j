from dev.langchain4j.observability.api.event import AiServiceCompletedEvent, ToolExecutedEvent
from jakarta.inject import Singleton
from micronaut.runtime.event.annotation import EventListener
from org.slf4j import LoggerFactory

LOG = LoggerFactory.getLogger("example.micronaut.aiservice.events.AiServiceAuditor")


@Singleton
class AiServiceAuditor:

    @EventListener  # <1>
    def onCompleted(self, event: AiServiceCompletedEvent) -> None:
        context = event.invocationContext()
        LOG.info("AI service {} completed invocation {}",
                 context.interfaceName(), context.invocationId())  # <2>

    @EventListener
    def onToolExecuted(self, event: ToolExecutedEvent) -> None:  # <3>
        LOG.info("Tool {} returned {}", event.request().name(), event.resultText())
