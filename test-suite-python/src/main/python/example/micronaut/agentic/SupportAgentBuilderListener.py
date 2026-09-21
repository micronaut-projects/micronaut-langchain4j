from dev.langchain4j.agentic.agent import AgentBuilder
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener


@Singleton
@Requires(env="agentic-docs")
class SupportAgentBuilderListener(BeanCreatedEventListener[AgentBuilder]):

    def onCreated(self, event: BeanCreatedEvent[AgentBuilder]) -> AgentBuilder:
        builder = event.getBean()
        builder.name("customer-support-agent")
        builder.outputKey("supportResponse")
        return builder
