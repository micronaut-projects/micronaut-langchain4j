package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.agent.AgentBuilder;
import dev.langchain4j.agentic.internal.AgentUtil;
import dev.langchain4j.internal.Json;
import io.micronaut.annotation.processing.test.JavaParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    void theOtherDeclarativeAgentsAreRegisteredForReflection() {
        Map<String, String> metadata = generateMetadata("test.Pitch", """
            package test;

            import dev.langchain4j.agentic.Agent;
            import dev.langchain4j.agentic.declarative.A2AClientAgent;
            import dev.langchain4j.agentic.declarative.HumanInTheLoop;
            import dev.langchain4j.agentic.declarative.McpClientAgent;
            import dev.langchain4j.agentic.declarative.RegistryAgent;
            import dev.langchain4j.agentic.declarative.SequenceAgent;
            import dev.langchain4j.service.V;
            import io.micronaut.langchain4j.agentic.annotation.AgenticService;

            @AgenticService
            interface Pitch {
                @SequenceAgent(subAgents = {Audience.class, Remote.class, Registered.class, Mcp.class, Writer.class}, outputKey = "pitch")
                String pitch(@V("topic") String topic);
            }

            interface Audience {
                @HumanInTheLoop(description = "Asks for the audience", outputKey = "audience")
                static String audience(@V("topic") String topic) {
                    return "kids";
                }
            }

            interface Remote {
                @A2AClientAgent(a2aServerUrl = "http://localhost:8080", outputKey = "remote")
                String remote(@V("topic") String topic);
            }

            interface Registered {
                @RegistryAgent("registered")
                String registered(@V("topic") String topic);
            }

            interface Mcp {
                @McpClientAgent(toolName = "tool", outputKey = "mcp")
                String mcp(@V("topic") String topic);
            }

            interface Writer {
                @Agent(outputKey = "pitch")
                String write(@V("topic") String topic);
            }
            """);
        assertEquals(java.util.Set.of("test.Pitch", "test.Audience", "test.Remote", "test.Registered", "test.Mcp", "test.Writer"), metadata.keySet());
        for (String type : java.util.List.of("test.Audience", "test.Remote", "test.Registered", "test.Mcp")) {
            String json = metadata.get(type);
            assertTrue(json.contains("{\"type\": \"" + type + "\", \"allPublicMethods\": true, \"allDeclaredMethods\": true}"), json);
        }
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

    @Test
    void structuredOutputsAndToolTypesAreRegisteredForTheirJsonSchema() {
        Map<String, String> metadata = generateMetadata("test.Model", """
            package test;

            import dev.langchain4j.agent.tool.Tool;
            import io.micronaut.langchain4j.annotation.AiService;
            import java.util.List;

            interface Model {
                @AiService
                interface Assistant {
                    List<Person> people(String text);
                }

                record Person(String name, Address address, Kind kind) {
                }

                class Address {
                    private String city;
                }

                enum Kind { FRIEND }

                @jakarta.inject.Singleton
                class Tools {
                    @Tool("Finds an order")
                    String find(Query query) {
                        return null;
                    }
                }

                record Query(String id) {
                }
            }
            """);
        String service = metadata.get("test.Model$Assistant");
        for (String type : new String[] {"Person", "Address", "Kind"}) {
            assertTrue(service.contains("{\"type\": \"test.Model$" + type
                + "\", \"allDeclaredFields\": true, \"allDeclaredConstructors\": true, \"allDeclaredMethods\": true}"), service);
        }
        String tools = metadata.get("test.Model$Tools");
        assertTrue(tools.contains("{\"type\": \"test.Model$Query\", \"allDeclaredFields\": true"), tools);
        assertFalse(tools.contains("proxy"), tools);
    }

    @Test
    void everyParameterizationOfAGenericRecordIsRegistered() {
        Map<String, String> metadata = generateMetadata("test.Model", """
            package test;

            import io.micronaut.langchain4j.annotation.AiService;

            interface Model {
                @AiService
                interface Assistant {
                    Box<Foo> foo(String text);

                    Box<Bar> bar(String text);
                }

                record Box<T>(T value) {
                }

                record Foo(String name) {
                }

                record Bar(int count) {
                }
            }
            """);
        String service = metadata.get("test.Model$Assistant");
        for (String type : new String[] {"Box", "Foo", "Bar"}) {
            assertTrue(service.contains("{\"type\": \"test.Model$" + type + "\", \"allDeclaredFields\": true"), service);
        }
        // the generic record is registered once
        assertEquals(service.indexOf("\"test.Model$Box\""), service.lastIndexOf("\"test.Model$Box\""), service);
    }

    @Test
    void independentParameterizationsAreNotBounded() {
        String service = generateMetadata("test.Model", SerdeImportVisitorTest.manyParameterizations(17)).get("test.Model$Assistant");
        for (int i = 1; i <= 17; i++) {
            assertTrue(service.contains("{\"type\": \"test.Model$Foo" + i + "\", \"allDeclaredFields\": true"), service);
        }
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

        List<List<String>> proxies = registeredProxies(json);

        // AgentBuilder.build, for agents
        assertTrue(proxies.contains(proxyOfWriter(AgentBuilder.interfacesToImplement(SampleAgent.class))), proxies::toString);
        // AgentUtil.buildAgent, for workflows
        Object workflow = AgentUtil.buildAgent(SampleAgent.class, (proxy, method, arguments) -> null);
        assertTrue(proxies.contains(proxyOfWriter(workflow.getClass().getInterfaces())), proxies::toString);
    }

    /**
     * @param json The generated metadata
     * @return The interfaces, in order, of each proxy the metadata registers
     */
    private static List<List<String>> registeredProxies(String json) {
        List<List<String>> proxies = new ArrayList<>();
        for (Object entry : (List<?>) Json.fromJson(json, Map.class).get("reflection")) {
            if (entry instanceof Map<?, ?> reflection && reflection.get("type") instanceof Map<?, ?> type
                && type.get("proxy") instanceof List<?> interfaces) {
                proxies.add(interfaces.stream().map(Object::toString).toList());
            }
        }
        return proxies;
    }

    /**
     * @param interfaces The interfaces of a proxy LangChain4j creates for {@link SampleAgent}, the agent type first
     * @return The interfaces of the same proxy for the type {@code test.Writer}
     */
    private static List<String> proxyOfWriter(Class<?>[] interfaces) {
        assertTrue(interfaces.length > 0, "LangChain4j created a proxy without interfaces");
        assertEquals(SampleAgent.class, interfaces[0], "The agent type is expected to be the first interface of the proxy");
        return Stream.concat(Stream.of("test.Writer"), Arrays.stream(interfaces).skip(1).map(Class::getName)).toList();
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
