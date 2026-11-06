package io.micronaut.langchain4j.agentic.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tools of an agentic service are offered to the model and invoked through their Micronaut
 * {@link io.micronaut.inject.ExecutableMethod}.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AgenticServiceToolInvocationTest.SPEC_NAME)
class AgenticServiceToolInvocationTest {

    static final String SPEC_NAME = "AgenticServiceToolInvocationTest";

    @Test
    void agentInvokesTool(WeatherAgent agent) {
        assertEquals("tool:Sunny in Paris", agent.forecast("Paris"));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AgenticService(tools = WeatherTools.class)
    public interface WeatherAgent {
        @UserMessage("What is the weather in {{city}}?")
        @Agent(description = "Forecasts the weather")
        String forecast(@V("city") String city);
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class WeatherTools {
        @Tool("Returns the weather of a city")
        String weather(@P("The city") String city) {
            return "Sunny in " + city;
        }
    }

    /**
     * Asks for the first tool it is offered with the city of the request, then answers with the result of the tool.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ScriptedChatModel implements ChatModel {

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            ChatMessage last = chatRequest.messages().getLast();
            if (last instanceof ToolExecutionResultMessage result) {
                return ChatResponse.builder().aiMessage(AiMessage.from("tool:" + result.text())).build();
            }
            ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name(chatRequest.toolSpecifications().getFirst().name())
                .arguments("{\"city\": \"Paris\"}")
                .build();
            return ChatResponse.builder().aiMessage(AiMessage.from(List.of(request))).build();
        }
    }
}
