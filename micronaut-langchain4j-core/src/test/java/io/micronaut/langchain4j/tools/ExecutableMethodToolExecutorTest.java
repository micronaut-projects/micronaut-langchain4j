package io.micronaut.langchain4j.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.SearchBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.exception.ToolArgumentsException;
import dev.langchain4j.exception.ToolExecutionException;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.IllegalConfigurationException;
import dev.langchain4j.service.tool.AiServiceTool;
import dev.langchain4j.service.tool.ToolExecutionResult;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The invocation of the tools through their {@link ExecutableMethod}, and the declarations rejected like LangChain4j
 * rejects them.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = ExecutableMethodToolExecutorTest.SPEC_NAME)
class ExecutableMethodToolExecutorTest {

    static final String SPEC_NAME = "ExecutableMethodToolExecutorTest";

    @Inject
    BeanContext beanContext;

    @Test
    void resultsAreConvertedLikeLangChain4jTools() {
        assertEquals("Success", execute("log", "{\"message\": \"x\"}").resultText());
        assertEquals("null", execute("nothing", "{}").resultText());
        assertEquals("{\"a\":1}", execute("map", "{}").resultText());
        Image image = Image.builder().url("https://example.com/image.png").build();
        assertEquals(List.of(ImageContent.from(image)), execute("image", "{}").resultContents());
        assertEquals(List.of(TextContent.from("one")), execute("content", "{}").resultContents());
        assertEquals(List.of(TextContent.from("a"), TextContent.from("b")), execute("contents", "{}").resultContents());
        assertEquals(List.of(TextContent.from("c")), execute("contentArray", "{}").resultContents());
    }

    @Test
    void asynchronousToolsAreComposed() {
        assertEquals("async:x", executor("async").executeAsync(request("async", "{\"value\": \"x\"}"), context()).join().resultText());
        assertEquals("42", executor("stage").executeAsync(request("stage", "{}"), context()).join().resultText());
        // invoked synchronously, the result of an asynchronous tool is waited for
        assertEquals("async:y", execute("async", "{\"value\": \"y\"}").resultText());
        assertEquals("Success", executor("log").executeAsync(request("log", "{\"message\": \"x\"}"), context()).join().resultText());
    }

    @Test
    void failuresAreToolExecutionExceptions() {
        assertThrows(ToolExecutionException.class, () -> execute("fail", "{}"));
        CompletionException asyncFailure = assertThrows(CompletionException.class,
            () -> executor("failedFuture").executeAsync(request("failedFuture", "{}"), context()).join());
        assertInstanceOf(ToolExecutionException.class, asyncFailure.getCause());
        assertThrows(ToolExecutionException.class, () -> execute("failedFuture", "{}"));
        CompletionException thrown = assertThrows(CompletionException.class,
            () -> executor("fail").executeAsync(request("fail", "{}"), context()).join());
        assertInstanceOf(ToolExecutionException.class, thrown.getCause());
        assertThrows(ToolArgumentsException.class, () -> execute("count", "{}"));
    }

    @Test
    void injectedParameters() {
        InvocationParameters parameters = new InvocationParameters();
        InvocationContext context = InvocationContext.builder().chatMemoryId("memory").invocationParameters(parameters).build();
        assertEquals("memory|true|true", executor("injected").executeWithContext(request("injected", "{}"), context).resultText());
        // the memory id given to the legacy execute method is the one of the invocation context
        assertTrue(executor("injected").execute(request("injected", "{}"), "other").startsWith("other|"));
    }

    @Test
    void specificationMetadataAndReturnBehavior() {
        AiServiceTool tool = tool("immediate");
        assertEquals(ReturnBehavior.IMMEDIATE, tool.returnBehavior());
        assertEquals(Map.of("version", 2, "searchBehavior", SearchBehavior.ALWAYS_VISIBLE), tool.toolSpecification().metadata());
        assertEquals(ReturnBehavior.TO_LLM, tool("log").returnBehavior());
    }

    @Test
    void invalidDeclarationsAreRejected() {
        assertInvalid(InvalidTools.class, "optionalPrimitive", "cannot be marked as @P(required = false)");
        assertInvalid(InvalidTools.class, "defaultedOptional", "Optional<T> already represents");
        assertInvalid(InvalidTools.class, "defaultedMemoryId", "framework-injected parameter");
        assertInvalid(InvalidTools.class, "unparsableDefault", "Cannot parse @P(defaultValue = \"x\")");
        assertInvalid(InvalidTools.class, "valueAndDescription", "both 'value' and 'description'");
    }

    private void assertInvalid(Class<?> type, String method, String message) {
        Exception e = assertThrows(Exception.class, () -> new ExecutableMethodToolExecutor(beanContext.getBean(type), method(type, method)));
        assertTrue(e instanceof IllegalConfigurationException || e instanceof IllegalArgumentException, e.toString());
        assertTrue(e.getMessage().contains(message), e.getMessage());
    }

    private ToolExecutionResult execute(String tool, String arguments) {
        return executor(tool).executeWithContext(request(tool, arguments), context());
    }

    private ExecutableMethodToolExecutor executor(String tool) {
        return new ExecutableMethodToolExecutor(beanContext.getBean(Tools.class), method(Tools.class, tool));
    }

    private AiServiceTool tool(String tool) {
        return ExecutableMethodToolExecutor.toAiServiceTool(beanContext.getBean(Tools.class), method(Tools.class, tool));
    }

    private ExecutableMethod<?, ?> method(Class<?> type, String name) {
        return beanContext.getBeanDefinition(type).getExecutableMethods().stream()
            .filter(method -> method.getMethodName().equals(name))
            .findFirst()
            .orElseThrow();
    }

    private static ToolExecutionRequest request(String name, String arguments) {
        return ToolExecutionRequest.builder().id("1").name(name).arguments(arguments).build();
    }

    private static InvocationContext context() {
        return InvocationContext.builder().chatMemoryId("default").build();
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Tools {

        @Tool("Logs a message")
        void log(String message) {
            // nothing to do: the test checks the result LangChain4j sends for a void tool
        }

        @Tool("Returns nothing")
        String nothing() {
            return null;
        }

        @Tool("Returns a map")
        Map<String, Integer> map() {
            return Map.of("a", 1);
        }

        @Tool("Returns an image")
        Image image() {
            return Image.builder().url("https://example.com/image.png").build();
        }

        @Tool("Returns a content")
        Content content() {
            return TextContent.from("one");
        }

        @Tool("Returns contents")
        List<Content> contents() {
            return List.of(TextContent.from("a"), TextContent.from("b"));
        }

        @Tool("Returns an array of contents")
        Content[] contentArray() {
            return new Content[] {TextContent.from("c")};
        }

        @Tool("Asynchronous")
        CompletableFuture<String> async(String value) {
            return CompletableFuture.supplyAsync(() -> "async:" + value);
        }

        @Tool("Completion stage")
        CompletionStage<Integer> stage() {
            return CompletableFuture.completedFuture(42);
        }

        @Tool("Fails")
        String fail() {
            throw new IllegalStateException("boom");
        }

        @Tool("Fails asynchronously")
        CompletableFuture<String> failedFuture() {
            return CompletableFuture.failedFuture(new IllegalStateException("boom"));
        }

        @Tool("Requires a count")
        int count(int count) {
            return count;
        }

        @Tool("Injected parameters")
        String injected(@ToolMemoryId Object memoryId, InvocationParameters parameters, InvocationContext context) {
            return memoryId + "|" + (parameters != null) + "|" + (context != null);
        }

        @Tool(value = "Returns immediately", returnBehavior = ReturnBehavior.IMMEDIATE,
            searchBehavior = SearchBehavior.ALWAYS_VISIBLE, metadata = "{\"version\": 2}")
        String immediate() {
            return "now";
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class InvalidTools {

        @Tool("Optional primitive")
        int optionalPrimitive(@P(value = "count", required = false) int count) {
            return count;
        }

        @Tool("Defaulted optional")
        String defaultedOptional(@P(value = "text", defaultValue = "x") Optional<String> text) {
            return text.orElse("");
        }

        @Tool("Defaulted memory id")
        String defaultedMemoryId(@ToolMemoryId @P(value = "memory", defaultValue = "x") Object memoryId) {
            return String.valueOf(memoryId);
        }

        @Tool("Unparsable default")
        int unparsableDefault(@P(value = "count", defaultValue = "x") int count) {
            return count;
        }

        @Tool("Value and description")
        String valueAndDescription(@P(value = "a", description = "b") String text) {
            return text;
        }
    }
}
