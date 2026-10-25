package example.micronaut.agentic

import dev.langchain4j.agentic.workflow.LoopAgentService
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Singleton
@Requires(env = "agentic-docs")
class LoopAgentServiceListener implements BeanCreatedEventListener<LoopAgentService<?>> {

    @Override
    LoopAgentService<?> onCreated(BeanCreatedEvent<LoopAgentService<?>> event) {
        LoopAgentService<?> builder = event.bean
        builder.exitCondition { scope, iteration -> iteration >= 3 }
        builder
    }
}
