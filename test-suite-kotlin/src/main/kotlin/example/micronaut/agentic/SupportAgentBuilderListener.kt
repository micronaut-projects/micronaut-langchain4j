package example.micronaut.agentic

import dev.langchain4j.agentic.agent.AgentBuilder
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["agentic-docs"])
internal class SupportAgentBuilderListener : BeanCreatedEventListener<AgentBuilder<*, *>> {

    override fun onCreated(event: BeanCreatedEvent<AgentBuilder<*, *>>): AgentBuilder<*, *> {
        val builder = event.bean
        builder.name("customer-support-agent")
        builder.outputKey("supportResponse")
        return builder
    }
}
