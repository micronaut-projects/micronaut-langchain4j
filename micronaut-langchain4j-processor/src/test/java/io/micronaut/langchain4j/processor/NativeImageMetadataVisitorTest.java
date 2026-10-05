package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micronaut.annotation.processing.test.JavaParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.stream.Collectors;
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
