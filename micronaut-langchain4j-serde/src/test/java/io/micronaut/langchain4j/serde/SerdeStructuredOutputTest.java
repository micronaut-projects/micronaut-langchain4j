package io.micronaut.langchain4j.serde;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The records of the structured outputs and of the tools are imported into Micronaut Serialization by
 * micronaut-langchain4j-processor, and mapped by LangChain4j with Micronaut Serialization.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = SerdeStructuredOutputTest.SPEC_NAME)
public class SerdeStructuredOutputTest {

    static final String SPEC_NAME = "SerdeStructuredOutputTest";

    @Inject
    Forecaster forecaster;

    @Inject
    ScriptedChatModel chatModel;

    @Test
    void recordsAreImported() {
        for (Class<?> type : List.of(Forecast.class, Location.class, WeatherQuery.class)) {
            assertTrue(BeanIntrospector.SHARED.findIntrospection(type).isPresent(), type.getName());
        }
    }

    @Test
    void structuredOutput() {
        chatModel.answer = "{\"location\": {\"city\": \"Paris\"}, \"temperature\": 21, \"conditions\": [\"sunny\", \"windy\"]}";
        assertEquals(new Forecast(new Location("Paris"), 21, List.of("sunny", "windy")), forecaster.forecast("Paris"));
    }

    @Test
    void recordToolArgumentsAndResults() {
        chatModel.answer = null;
        assertEquals("tool:{\"location\":{\"city\":\"Paris\"},\"temperature\":21,\"conditions\":[\"sunny\"]}",
            forecaster.chat("What is the weather in Paris?"));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService(tools = WeatherTools.class)
    public interface Forecaster {
        Forecast forecast(String city);

        String chat(String message);
    }

    public record Location(String city) {
    }

    public record Forecast(Location location, int temperature, List<String> conditions) {
    }

    public record WeatherQuery(Location location) {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    public static class WeatherTools {
        @Tool("Returns the weather forecast")
        public Forecast weather(WeatherQuery query) {
            return new Forecast(query.location(), 21, List.of("sunny"));
        }
    }

    /**
     * Answers with the configured JSON, or asks for the weather tool and answers with its result.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ScriptedChatModel implements ChatModel {

        volatile String answer;

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            ChatMessage last = chatRequest.messages().getLast();
            if (last instanceof ToolExecutionResultMessage result) {
                return ChatResponse.builder().aiMessage(AiMessage.from("tool:" + result.text())).build();
            }
            if (answer != null) {
                return ChatResponse.builder().aiMessage(AiMessage.from(answer)).build();
            }
            ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name("weather")
                .arguments("{\"query\": {\"location\": {\"city\": \"Paris\"}}}")
                .build();
            return ChatResponse.builder().aiMessage(AiMessage.from(List.of(request))).build();
        }
    }
}
