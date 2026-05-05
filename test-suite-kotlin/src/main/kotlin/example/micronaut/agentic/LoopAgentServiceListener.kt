package example.micronaut.agentic

import dev.langchain4j.agentic.workflow.LoopAgentService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Singleton
@Requires(env = ["agentic-docs"])
internal class LoopAgentServiceListener : BeanCreatedEventListener<LoopAgentService<*>> {

    override fun onCreated(event: BeanCreatedEvent<LoopAgentService<*>>): LoopAgentService<*> {
        val builder = event.bean
        builder.exitCondition { _, iteration -> iteration >= 3 }
        return builder
    }
}
