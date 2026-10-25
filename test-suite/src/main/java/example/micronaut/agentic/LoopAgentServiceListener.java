package example.micronaut.agentic;

import dev.langchain4j.agentic.workflow.LoopAgentService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

@Singleton
@Requires(env = "agentic-docs")
final class LoopAgentServiceListener implements BeanCreatedEventListener<LoopAgentService<?>> {

    @Override
    public LoopAgentService<?> onCreated(@NonNull BeanCreatedEvent<LoopAgentService<?>> event) {
        LoopAgentService<?> builder = event.getBean();
        builder.exitCondition((scope, iteration) -> iteration >= 3);
        return builder;
    }
}
