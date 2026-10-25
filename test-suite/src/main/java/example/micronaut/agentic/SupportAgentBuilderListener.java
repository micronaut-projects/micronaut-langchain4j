package example.micronaut.agentic;

import dev.langchain4j.agentic.agent.AgentBuilder;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

@Singleton
@Requires(env = "agentic-docs")
final class SupportAgentBuilderListener implements BeanCreatedEventListener<AgentBuilder<?, ?>> {

    @Override
    public AgentBuilder<?, ?> onCreated(@NonNull BeanCreatedEvent<AgentBuilder<?, ?>> event) {
        AgentBuilder<?, ?> builder = event.getBean();
        builder.name("customer-support-agent");
        builder.outputKey("supportResponse");
        return builder;
    }
}
