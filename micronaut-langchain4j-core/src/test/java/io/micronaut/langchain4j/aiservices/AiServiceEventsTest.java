package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.event.AiServiceEvent;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import dev.langchain4j.observability.api.event.InputGuardrailExecutedEvent;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;
import dev.langchain4j.observability.api.listener.AiServiceCompletedListener;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.tool.ToolProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = AiServiceEventsTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServiceEventsTest {

    static final String SPEC_NAME = "AiServiceEventsTest";

    @Inject
    ObservedAssistant assistant;

    @Inject
    EventRecorder recorder;

    @Inject
    AllEventsListener allEventsListener;

    @Inject
    CompletedListener completedListener;

    @Test
    void publishesTheAiServiceEventsAsApplicationEvents() {
        assertEquals("tool said: sunny", assistant.chat("weather?"));

        assertEquals(List.of("started", "guardrail", "tool weather", "completed"), recorder.events);
        assertTrue(allEventsListener.events.stream().anyMatch(AiServiceStartedEvent.class::isInstance));
        assertTrue(allEventsListener.events.stream().anyMatch(ToolExecutedEvent.class::isInstance));
        assertEquals(1, completedListener.events.size());
        String invocationId = completedListener.events.getFirst().invocationContext().invocationId().toString();
        assertTrue(allEventsListener.events.stream().allMatch(e -> e.invocationContext().invocationId().toString().equals(invocationId)));
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel() {
            return new ToolCallingChatModel();
        }

        @Singleton
        ToolProvider weatherTools() {
            return AiServiceToolProviderTest.toolProvider("weather", "sunny");
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class EventRecorder {
        final List<String> events = new CopyOnWriteArrayList<>();

        @EventListener
        void onStarted(AiServiceStartedEvent event) {
            events.add("started");
        }

        @EventListener
        void onGuardrail(InputGuardrailExecutedEvent event) {
            events.add("guardrail");
        }

        @EventListener
        void onTool(ToolExecutedEvent event) {
            events.add("tool " + event.request().name());
        }

        @EventListener
        void onCompleted(AiServiceCompletedEvent event) {
            events.add("completed");
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class AllEventsListener implements ApplicationEventListener<AiServiceEvent> {
        final List<AiServiceEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void onApplicationEvent(AiServiceEvent event) {
            events.add(event);
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class CompletedListener implements AiServiceCompletedListener {
        final List<AiServiceCompletedEvent> events = new CopyOnWriteArrayList<>();

        @Override
        public void onEvent(AiServiceCompletedEvent event) {
            events.add(event);
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class AllowAll implements InputGuardrail {
        @Override
        public InputGuardrailResult validate(InputGuardrailRequest request) {
            return success();
        }
    }
}

@Requires(property = "spec.name", value = AiServiceEventsTest.SPEC_NAME)
@AiService
@InputGuardrails(AiServiceEventsTest.AllowAll.class)
interface ObservedAssistant {
    String chat(String userMessage);
}
