import java
from dev.langchain4j.agentic.agent import AgentBuilder
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener


# TODO(python): `BeanCreatedEventListener[AgentBuilder]` cannot be compiled (the self-referential type bound
# `AgentBuilder<T, B extends AgentBuilder<T, ?>>` overflows the stub generator), so the listener is untyped and
# filters the created beans itself.
@Singleton
@Requires(env="agentic-docs")
class SupportAgentBuilderListener(BeanCreatedEventListener):

    def onCreated(self, event: BeanCreatedEvent):
        builder = event.getBean()
        if java.instanceof(builder, AgentBuilder):
            builder.name("customer-support-agent")
            builder.outputKey("supportResponse")
        return builder
