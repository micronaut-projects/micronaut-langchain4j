package io.micronaut.langchain4j.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The tool specifications and the coercion of the tool arguments re-implement helpers of LangChain4j that are not
 * public: the same tool methods are specified and invoked by LangChain4j ({@link ToolSpecifications},
 * {@link DefaultToolExecutor}) and through their {@link ExecutableMethod}, and must give the same results. This fails
 * when a LangChain4j upgrade changes its rules.
 */
@MicronautTest(startApplication = false)
@Property(name = "spec.name", value = LangChain4jToolParityTest.SPEC_NAME)
class LangChain4jToolParityTest {

    static final String SPEC_NAME = "LangChain4jToolParityTest";

    private static final Map<String, List<String>> ARGUMENTS = Map.of(
        "numbers", List.of(
            "{\"i\": 1, \"boxed\": 2, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": 8.25, \"integer\": 9}",
            "{\"i\": \"1\", \"boxed\": \"2\", \"l\": \"9007199254740993\", \"d\": \"4.5\", \"f\": \"5.5\", \"s\": \"6\", \"b\": \"7\", \"decimal\": \" 8.25 \", \"integer\": \"12345678901234567890\"}",
            "{\"i\": 1.0, \"boxed\": 2.0, \"l\": 3.0, \"d\": 4, \"f\": 5, \"s\": 6.0, \"b\": 7.0, \"decimal\": 8, \"integer\": 9.0}",
            "{\"i\": 1.5, \"boxed\": 2, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": 8.25, \"integer\": 9}",
            "{\"i\": 3000000000, \"boxed\": 2, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": 8.25, \"integer\": 9}",
            "{\"i\": 1, \"boxed\": 2, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 300, \"decimal\": 8.25, \"integer\": 9}",
            "{\"i\": 1, \"boxed\": 2, \"l\": 3, \"d\": \"x\", \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": 8.25, \"integer\": 9}",
            "{\"i\": 1, \"boxed\": null, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": null, \"integer\": null}",
            "{\"i\": true, \"boxed\": 2, \"l\": 3, \"d\": 4.5, \"f\": 5.5, \"s\": 6, \"b\": 7, \"decimal\": 8.25, \"integer\": 9}",
            "{}"),
        "text", List.of(
            "{\"text\": \"a\", \"unit\": \"CELSIUS\", \"flag\": true, \"id\": \"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\"}",
            "{\"text\": 1, \"unit\": \" celsius \", \"flag\": false, \"id\": \"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\"}",
            "{\"text\": \"a\", \"unit\": \"kelvin\", \"flag\": true, \"id\": \"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\"}",
            "{\"text\": \"a\", \"unit\": \"CELSIUS\", \"flag\": \"true\", \"id\": \"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\"}",
            "{\"text\": \"a\", \"unit\": \"CELSIUS\", \"flag\": true, \"id\": \"not a uuid\"}",
            "{\"text\": null, \"unit\": null, \"flag\": true, \"id\": null}",
            "{\"text\": \"a\", \"unit\": \"CELSIUS\", \"flag\": true, \"id\": \"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\",}",
            "\"{\\\"text\\\": \\\"a\\\", \\\"unit\\\": \\\"CELSIUS\\\", \\\"flag\\\": true, \\\"id\\\": \\\"8c6ad8a4-8a51-4d0f-9f3c-0a4f5a3c1b2d\\\"}\"",
            "not json",
            ""),
        "structures", List.of(
            "{\"list\": [1, 2], \"map\": {\"k\": \"v\"}, \"point\": {\"x\": 1, \"y\": 2}, \"units\": [\"CELSIUS\"]}",
            "{\"list\": [\"1\", 2.0], \"map\": {\"k\": 1}, \"point\": \"{\\\"x\\\": 3, \\\"y\\\": 4}\", \"units\": [\"CELSIUS\", \"FAHRENHEIT\"]}",
            "{\"list\": [], \"map\": {}, \"point\": {}, \"units\": []}",
            "{\"list\": null, \"map\": null, \"point\": null, \"units\": null}",
            "{\"list\": 1, \"map\": {\"k\": \"v\"}, \"point\": {\"x\": 1, \"y\": 2}, \"units\": [\"CELSIUS\"]}",
            "{\"list\": [1, 2], \"map\": {\"k\": \"v\"}, \"point\": {\"z\": 1}, \"units\": [\"kelvin\"]}"),
        "named", List.of(
            "{\"a\": \"A\", \"b2\": \"B\", \"c\": \"C\", \"d\": 4, \"e\": \"E\"}",
            "{\"a\": \"A\", \"b2\": \"B\"}",
            "{\"a\": \"A\", \"b2\": \"B\", \"d\": \"7\", \"e\": null}",
            "{\"a\": \"A\", \"b\": \"B\"}",
            "{}"),
        "nothing", List.of("{}", "{\"unexpected\": 1}"),
        "blank", List.of("{\"input\": \"a\"}", "{}")
    );

    @Inject
    BeanContext beanContext;

    @Test
    void specificationsAreTheOnesLangChain4jBuilds() {
        for (Method method : toolMethods()) {
            assertEquals(ToolSpecifications.toolSpecificationFrom(method), executor(method).toolSpecification(), method.getName());
        }
    }

    @Test
    void argumentsAreCoercedAndResultsConvertedLikeLangChain4j() {
        ParityTools plainObject = new ParityTools();
        List<String> differences = new ArrayList<>();
        for (Method method : toolMethods()) {
            // as LangChain4j builds the executor of a tool of an AI service
            ToolExecutor langChain4j = DefaultToolExecutor.builder()
                .object(plainObject)
                .originalMethod(method)
                .methodToInvoke(method)
                .wrapToolArgumentsExceptions(true)
                .propagateToolExecutionExceptions(true)
                .build();
            ToolExecutor micronaut = executor(method);
            List<String> payloads = ARGUMENTS.get(method.getName());
            assertNotNull(payloads, () -> "No arguments are declared in ARGUMENTS for the tool method " + method.getName());
            for (String arguments : payloads) {
                String expected = outcome(langChain4j, method, arguments);
                String actual = outcome(micronaut, method, arguments);
                if (!expected.equals(actual)) {
                    differences.add(method.getName() + " " + arguments + "\n  LangChain4j: " + expected + "\n  Micronaut:   " + actual);
                }
            }
        }
        assertEquals(List.of(), differences, () -> String.join("\n", differences));
    }

    private static String outcome(ToolExecutor executor, Method method, String arguments) {
        ToolExecutionRequest request = ToolExecutionRequest.builder().id("1").name(method.getName()).arguments(arguments).build();
        try {
            return "result: " + executor.executeWithContext(request, InvocationContext.builder().chatMemoryId("memory").build()).resultText();
        } catch (RuntimeException e) {
            // the messages are not compared: they are worded for the model, and may be reworded
            return e.getClass().getName();
        }
    }

    private static List<Method> toolMethods() {
        List<Method> methods = Arrays.stream(ParityTools.class.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Tool.class))
            .toList();
        for (Method method : methods) {
            for (Parameter parameter : method.getParameters()) {
                // LangChain4j names the tool parameters after the reflective parameter names
                assertTrue(parameter.isNamePresent(), () -> "The tests must be compiled with -parameters: the parameters of "
                    + method.getName() + " have no names");
            }
        }
        return methods;
    }

    private ExecutableMethodToolExecutor executor(Method method) {
        ExecutableMethod<?, ?> executableMethod = beanContext.getBeanDefinition(ParityTools.class).getExecutableMethods().stream()
            .filter(candidate -> candidate.getMethodName().equals(method.getName())
                && Arrays.equals(candidate.getArgumentTypes(), method.getParameterTypes()))
            .findFirst()
            .orElseThrow();
        return new ExecutableMethodToolExecutor(beanContext.getBean(ParityTools.class), executableMethod);
    }

    enum Unit {
        CELSIUS,
        FAHRENHEIT
    }

    record Point(int x, int y) {
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ParityTools {

        @Tool("Numbers")
        String numbers(int i, Integer boxed, long l, double d, float f, short s, byte b, BigDecimal decimal, BigInteger integer) {
            return String.join("|", List.of("" + i, "" + boxed, "" + l, "" + d, "" + f, "" + s, "" + b, "" + decimal, "" + integer));
        }

        @Tool("Text")
        String text(String text, Unit unit, boolean flag, UUID id) {
            return text + "|" + unit + "|" + flag + "|" + id;
        }

        @Tool("Structures")
        String structures(List<Integer> list, Map<String, String> map, Point point, Set<Unit> units) {
            return list + "|" + map + "|" + point + "|" + units;
        }

        @Tool(name = "named", value = {"Line one", "Line two"})
        String named(@P("The first") String a,
                     @P(name = "b2", description = "The second") String b,
                     @P(value = "The third", required = false) String c,
                     @P(value = "The fourth", defaultValue = "5") int d,
                     Optional<String> e,
                     @ToolMemoryId String memoryId) {
            return a + "|" + b + "|" + c + "|" + d + "|" + e + "|" + memoryId;
        }

        @Tool(name = " ", value = "Blank name")
        String blank(@P(name = " ", value = " ", description = "The input") String input) {
            return input;
        }

        @Tool
        void nothing() {
            // nothing to do: the test compares the result of a void tool
        }
    }
}
