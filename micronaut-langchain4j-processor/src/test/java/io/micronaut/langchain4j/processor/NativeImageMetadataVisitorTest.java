package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.internal.AgentUtil;
import io.micronaut.annotation.processing.test.JavaParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class NativeImageMetadataVisitorTest {

    private static final String PREFIX = "META-INF/native-image/io.micronaut.langchain4j/";

    @Test
    void aiServiceIsRegisteredForReflectionAndProxy() {
        Map<String, String> metadata = generateMetadata("test.Friend", """
            package test;

            import dev.langchain4j.service.SystemMessage;
            import io.micronaut.langchain4j.annotation.AiService;

            @AiService
            interface Friend {
                @SystemMessage("You are a friend")
                String chat(String message);
            }
            """);
        String json = metadata.get("test.Friend");
        assertTrue(json.contains("{\"type\": \"test.Friend\", \"allPublicMethods\": true, \"allDeclaredMethods\": true}"), json);
        assertTrue(json.contains("{\"type\": {\"proxy\": [\"test.Friend\"]}}"), json);
        assertFalse(json.contains("InternalAgent"), json);
    }

    @Test
    void agentsAndWorkflowsAreRegisteredForTheAgenticProxies() {
        Map<String, String> metadata = generateMetadata("test.Story", """
            package test;

            import dev.langchain4j.agentic.Agent;
            import dev.langchain4j.agentic.declarative.SequenceAgent;
            import dev.langchain4j.service.V;
            import io.micronaut.langchain4j.agentic.annotation.AgenticService;

            @AgenticService
            interface Story {
                @SequenceAgent(subAgents = Writer.class, outputKey = "story")
                String write(@V("topic") String topic);
            }

            interface Writer {
                @Agent(outputKey = "story")
                String write(@V("topic") String topic);
            }

            interface Unrelated {
                String nothing();
            }
            """);
        assertEquals(java.util.Set.of("test.Story", "test.Writer"), metadata.keySet());
        String json = metadata.get("test.Writer");
        assertTrue(json.contains("{\"type\": {\"proxy\": [\"test.Writer\", \"dev.langchain4j.agentic.internal.InternalAgent\", "
            + "\"dev.langchain4j.agentic.internal.AgenticScopeOwner\", \"dev.langchain4j.service.memory.ChatMemoryAccess\", "
            + "\"dev.langchain4j.agentic.agent.ChatMessagesAccess\", "
            + "\"dev.langchain4j.observability.api.listener.AiServiceResponseReceivedListener\"]}}"), json);
        assertTrue(json.contains("{\"type\": {\"proxy\": [\"test.Writer\", \"dev.langchain4j.agentic.internal.InternalAgent\", "
            + "\"dev.langchain4j.agentic.internal.AgenticScopeOwner\", \"dev.langchain4j.agentic.scope.AgenticScopeAccess\"]}}"), json);
        assertTrue(json.contains("{\"type\": \"dev.langchain4j.agentic.internal.InternalAgent\", \"allPublicMethods\": true}"), json);
    }

    @Test
    void toolClassesNeedNoMetadata() {
        Map<String, String> metadata = generateMetadata("test.Tools", """
            package test;

            import dev.langchain4j.agent.tool.Tool;

            @jakarta.inject.Singleton
            class Tools {
                @Tool("Adds two numbers")
                int add(int a, int b) {
                    return a + b;
                }
            }
            """);
        assertTrue(metadata.isEmpty(), metadata::toString);
    }

    /**
     * The proxy interface lists are copied from LangChain4j, which has no metadata of its own: this fails when an
     * upgrade changes the proxies LangChain4j creates, which otherwise only a native image would show.
     */
    @Test
    void theRegisteredProxiesAreTheOnesLangChain4jCreates() {
        String json = generateMetadata("test.Writer", """
            package test;

            import dev.langchain4j.agentic.Agent;

            interface Writer {
                @Agent
                String write(String topic);
            }
            """).get("test.Writer");

        // AgentBuilder.build, for agents
        assertTrue(json.contains(proxy(AgentBuilder.interfacesToImplement(SampleAgent.class))), json);
        // AgentUtil.buildAgent, for workflows
        Object workflow = AgentUtil.buildAgent(SampleAgent.class, (proxy, method, arguments) -> null);
        assertTrue(json.contains(proxy(workflow.getClass().getInterfaces())), json);
    }

    /**
     * @param interfaces The interfaces of a proxy LangChain4j creates for {@link SampleAgent}, the agent type first
     * @return The metadata of the same proxy for the type {@code test.Writer}
     */
    private static String proxy(Class<?>[] interfaces) {
        assertEquals(SampleAgent.class, interfaces[0]);
        return "{\"type\": {\"proxy\": [" + Stream.concat(Stream.of("test.Writer"), Arrays.stream(interfaces).skip(1).map(Class::getName))
            .map(name -> '"' + name + '"')
            .collect(Collectors.joining(", ")) + "]}}";
    }

    interface SampleAgent {
        @Agent
        String write(String topic);
    }

    private static Map<String, String> generateMetadata(String className, String source) {
        try (JavaParser parser = new JavaParser()) {
            Iterable<? extends JavaFileObject> generated = parser.generate(className, source);
            return StreamSupport.stream(generated.spliterator(), false)
                .filter(file -> file.getName().contains(PREFIX) && file.getName().endsWith("/reachability-metadata.json"))
                .collect(Collectors.toMap(
                    file -> {
                        String name = file.getName();
                        String type = name.substring(name.indexOf(PREFIX) + PREFIX.length());
                        return type.substring(0, type.indexOf('/'));
                    },
                    NativeImageMetadataVisitorTest::read));
        }
    }

    private static String read(JavaFileObject file) {
        try {
            return file.getCharContent(true).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
