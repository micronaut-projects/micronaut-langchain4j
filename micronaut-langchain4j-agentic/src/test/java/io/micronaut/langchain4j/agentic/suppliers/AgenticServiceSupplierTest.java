package io.micronaut.langchain4j.agentic.suppliers;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.ChatModelSupplier;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.agentic.declarative.ToolsSupplier;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The parameters of the static supplier methods of an agent are Micronaut beans.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = AgenticServiceSupplierTest.SPEC_NAME)
class AgenticServiceSupplierTest {

    static final String SPEC_NAME = "AgenticServiceSupplierTest";

    @Test
    void suppliersReceiveBeans(WeatherWorkflow workflow) {
        assertEquals("forecaster:Sunny in Paris", workflow.forecast("Paris"));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AgenticService
    public interface WeatherWorkflow {
        @SequenceAgent(outputKey = "forecast", subAgents = WeatherAgent.class)
        String forecast(@V("city") String city);
    }

    public interface WeatherAgent {
        @UserMessage("What is the weather in {{city}}?")
        @Agent(description = "Forecasts the weather", outputKey = "forecast")
        String forecast(@V("city") String city);

        @ChatModelSupplier
        static ChatModel chatModel(@Named("forecaster") ChatModel chatModel) {
            return chatModel;
        }

        @ToolsSupplier
        static Object tools(WeatherTools tools) {
            return tools;
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    public static class WeatherTools {
        @Tool("Returns the weather of a city")
        public String weather(@P("The city") String city) {
            return "Sunny in " + city;
        }
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ChatModels {

        @Singleton
        @Named("forecaster")
        ChatModel forecaster() {
            return new ToolCallingChatModel("forecaster");
        }

        @Singleton
        @Named("other")
        ChatModel other() {
            return new ToolCallingChatModel("other");
        }
    }

    record ToolCallingChatModel(String name) implements ChatModel {
        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            if (chatRequest.messages().getLast() instanceof ToolExecutionResultMessage result) {
                return ChatResponse.builder().aiMessage(AiMessage.from(name + ":" + result.text())).build();
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
