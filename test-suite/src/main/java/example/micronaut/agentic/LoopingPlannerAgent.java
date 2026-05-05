package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.LoopAgent;
import dev.langchain4j.agentic.workflow.LoopAgentService;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import jakarta.inject.Singleton;

/**
 * Demonstrates declarative LoopAgent orchestration using Micronaut DI.
 */
@AgenticService(outputKey = "translation")
public interface LoopingPlannerAgent {

    @LoopAgent(
        subAgents = {
            TranslatorAgent.class
        },
        outputKey = "text",
        maxIterations = 3
    )
    String translatesInLoop(@V("text") String text);

    interface TranslatorAgent {
        @UserMessage("""
            You are a translator.
            If the text is in English, translate to French.
            If the text is in French, translate to German.
            If the text is in German, translate to Spanish.
            Translate this: "{{text}}". Answer with the translation only, no explanations, no details.
            """)
        @Agent(outputKey = "text")
        String translate(@V("text") String text);
    }

    @Singleton
    class LoopBuilderListener implements BeanCreatedEventListener<LoopAgentService<?>> {

        @Override
        public LoopAgentService<?> onCreated(@NonNull BeanCreatedEvent<LoopAgentService<?>> event) {
            LoopAgentService<?> builder = event.getBean();
            builder.exitCondition((scope, idx) -> idx == 3);
            return builder;
        }
    }
}
