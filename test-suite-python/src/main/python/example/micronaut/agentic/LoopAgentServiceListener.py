from dev.langchain4j.agentic.workflow import LoopAgentService
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener


@Singleton
@Requires(env="agentic-docs")
class LoopAgentServiceListener(BeanCreatedEventListener[LoopAgentService]):

    def onCreated(self, event: BeanCreatedEvent[LoopAgentService]) -> LoopAgentService:
        builder = event.getBean()
        builder.exitCondition(lambda scope, iteration: iteration >= 3)
        return builder
