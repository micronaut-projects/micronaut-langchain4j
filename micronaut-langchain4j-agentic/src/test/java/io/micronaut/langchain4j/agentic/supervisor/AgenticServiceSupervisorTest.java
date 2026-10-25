package io.micronaut.langchain4j.agentic.supervisor;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.V;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A supervisor agent plans which of its sub-agents to invoke with the Micronaut chat model.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AgenticServiceSupervisorTest.SPEC_NAME)
class AgenticServiceSupervisorTest {

    static final String SPEC_NAME = "AgenticServiceSupervisorTest";

    @Test
    void supervisorInvokesTheSubAgents(TravelSupervisor supervisor, ScriptedChatModel model) {
        assertEquals("Pack sunglasses: Sunny in Paris", supervisor.plan("I am going to Paris"));
        assertEquals(List.of("planner", "weatherExpert", "planner"), model.calls);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AgenticService
    public interface TravelSupervisor {
        @SupervisorAgent(subAgents = WeatherExpert.class, responseStrategy = SupervisorResponseStrategy.SUMMARY)
        String plan(@V("request") String request);
    }

    public interface WeatherExpert {
        @dev.langchain4j.service.UserMessage("What is the weather in {{city}}?")
        @Agent(name = "weatherExpert", description = "Forecasts the weather of a city")
        String forecast(@V("city") String city);
    }

    /**
     * Plays both the planner of the supervisor and the weather expert.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ScriptedChatModel implements ChatModel {

        final List<String> calls = Collections.synchronizedList(new ArrayList<>());

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            boolean planner = chatRequest.messages().stream()
                .anyMatch(message -> message instanceof SystemMessage system && system.text().contains("planner expert"));
            String user = lastUserMessage(chatRequest.messages());
            if (!planner) {
                calls.add("weatherExpert");
                return answer(user.contains("Paris") ? "Sunny in Paris" : "Unknown");
            }
            calls.add("planner");
            if (user.contains("Sunny in Paris")) {
                return answer("""
                    {"agentName": "done", "arguments": {"response": "Pack sunglasses: Sunny in Paris"}}""");
            }
            return answer("""
                {"agentName": "weatherExpert", "arguments": {"city": "Paris"}}""");
        }

        private static String lastUserMessage(List<ChatMessage> messages) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i) instanceof UserMessage user) {
                    return user.singleText();
                }
            }
            return "";
        }

        private static ChatResponse answer(String text) {
            return ChatResponse.builder().aiMessage(AiMessage.from(text)).build();
        }
    }
}
