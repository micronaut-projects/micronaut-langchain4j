package io.micronaut.langchain4j.tools;

import dev.langchain4j.agent.tool.CompensateFor;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonIntegerSchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonStringSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.tool.AiServiceTool;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tools of an AI service are specified from the Micronaut metadata of the {@link Tool} methods and invoked
 * through their {@link io.micronaut.inject.ExecutableMethod}, without LangChain4j scanning the tool class.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = ExecutableMethodToolsTest.SPEC_NAME)
class ExecutableMethodToolsTest {

    static final String SPEC_NAME = "ExecutableMethodToolsTest";

    @Inject
    ToolRegistry toolRegistry;

    @Inject
    BeanContext beanContext;

    @Inject
    ScriptedChatModel chatModel;

    @Inject
    Assistant assistant;

    @Test
    void toolSpecificationsAreBuiltFromTheAnnotationMetadata() {
        Map<String, ToolSpecification> specifications = toolRegistry.getAiServiceTools(Set.of(Calculator.class)).stream()
            .map(AiServiceTool::toolSpecification)
            .collect(Collectors.toMap(ToolSpecification::name, Function.identity()));

        assertEquals(Set.of("add", "greet", "fail", "crash", "current_unit"), specifications.keySet());

        ToolSpecification add = specifications.get("add");
        assertEquals("Adds two numbers", add.description());
        JsonObjectSchema parameters = add.parameters();
        assertEquals(List.of("first", "b"), new ArrayList<>(parameters.properties().keySet()));
        assertInstanceOf(JsonIntegerSchema.class, parameters.properties().get("first"));
        assertEquals("The first number", parameters.properties().get("first").description());
        assertEquals("The second number", parameters.properties().get("b").description());
        assertEquals(List.of("first", "b"), parameters.required());

        // @ToolMemoryId is not a parameter of the tool, Optional and defaulted parameters are not required
        JsonObjectSchema greet = specifications.get("greet").parameters();
        assertEquals(List.of("name", "greeting", "suffix"), new ArrayList<>(greet.properties().keySet()));
        assertInstanceOf(JsonStringSchema.class, greet.properties().get("greeting"));
        assertEquals(List.of("name"), greet.required());

        assertNull(specifications.get("current_unit").parameters());
        assertEquals("Line one\nLine two", specifications.get("current_unit").description());
    }

    @Test
    void toolArgumentsAreCoercedAndTheMethodInvoked() {
        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("add").arguments("{\"first\": \"2\", \"b\": 3}").build();
        assertEquals("tool:5", assistant.chat("memory-1", "What is 2 + 3?"));
    }

    @Test
    void injectedParametersAndDefaultValues() {
        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("greet").arguments("{\"name\": \"Fred\"}").build();
        assertEquals("tool:Hello Fred (memory-2)", assistant.chat("memory-2", "Greet Fred"));

        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("greet")
            .arguments("{\"name\": \"Fred\", \"greeting\": \"Hi\", \"suffix\": \"!\"}").build();
        assertEquals("tool:Hi Fred! (memory-3)", assistant.chat("memory-3", "Greet Fred"));
    }

    @Test
    void toolErrorsAreReportedToTheModel() {
        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("fail").arguments("{}").build();
        assertEquals("tool:boom", assistant.chat("memory-4", "Fail"));
    }

    @Test
    void toolErrorsThrownAsErrorsAreReportedToTheModel() {
        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("crash").arguments("{}").build();
        assertEquals("tool:crashed", assistant.chat("memory-6", "Crash"));
    }

    @Test
    void compensatingActionsAreFoundToBeReportedAndAreNotTools() {
        BeanDefinition<Booking> definition = beanContext.getBeanDefinition(Booking.class);

        assertEquals(List.of("cancelFlight"), ToolRegistry.compensatingMethods(definition).stream()
            .map(ExecutableMethod::getMethodName)
            .toList());
        assertEquals(List.of("bookFlight"), toolRegistry.getAiServiceTools(Set.of(Booking.class)).stream()
            .map(tool -> tool.toolSpecification().name())
            .toList());
        assertTrue(ToolRegistry.compensatingMethods(beanContext.getBeanDefinition(Calculator.class)).isEmpty());
    }

    @Test
    void invalidArgumentsFailLikeLangChain4jTools() {
        // without a ToolArgumentsErrorHandler, LangChain4j rethrows the argument error of a tool
        chatModel.toolRequest = ToolExecutionRequest.builder().id("1").name("add").arguments("{\"first\": \"two\", \"b\": 3}").build();
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> assistant.chat("memory-5", "What is two + 3?"));
        assertTrue(e.getMessage().contains("\"first\""), e.getMessage());
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService(tools = Calculator.class)
    interface Assistant {
        String chat(@MemoryId String memoryId, @UserMessage String message);
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Calculator {

        @Tool("Adds two numbers")
        int add(@P("The first number") int first, @P(name = "b", description = "The second number") int second) {
            return first + second;
        }

        @Tool("Greets someone")
        String greet(String name,
                     Optional<String> greeting,
                     @P(value = "The suffix", defaultValue = "") String suffix,
                     @ToolMemoryId Object memoryId) {
            return greeting.orElse("Hello") + " " + name + suffix + " (" + memoryId + ")";
        }

        @Tool("Always fails")
        String fail() {
            throw new IllegalStateException("boom");
        }

        @Tool("Always fails with an error")
        String crash() {
            throw new AssertionError("crashed");
        }

        @Tool(name = "current_unit", value = {"Line one", "Line two"})
        String unit() {
            return "metric";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Booking {

        @Tool("Books a flight")
        String bookFlight(String flight) {
            return "booked " + flight;
        }

        @CompensateFor("bookFlight")
        void cancelFlight(String flight) {
            // not registered as a compensating action: the test checks that it is found to be reported
        }
    }

    /**
     * Asks for the configured tool, then answers with the result of the tool.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ScriptedChatModel implements ChatModel {

        volatile ToolExecutionRequest toolRequest;

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            ChatMessage last = chatRequest.messages().getLast();
            if (last instanceof ToolExecutionResultMessage result) {
                return ChatResponse.builder().aiMessage(AiMessage.from("tool:" + result.text())).build();
            }
            return ChatResponse.builder().aiMessage(AiMessage.from(List.of(toolRequest))).build();
        }
    }
}
