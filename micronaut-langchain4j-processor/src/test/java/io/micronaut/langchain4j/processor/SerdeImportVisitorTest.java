package io.micronaut.langchain4j.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micronaut.annotation.processing.test.JavaParser;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

/**
 * The public records mapped to and from JSON by LangChain4j are imported into Micronaut Serialization.
 */
class SerdeImportVisitorTest {

    private static final Pattern INTROSPECTION = Pattern.compile(".*/\\$test_Model\\$(\\w+)\\$Introspection\\.class");

    @Test
    void recordsOfStructuredOutputsAndToolsAreImported() {
        Set<String> introspected = introspectedTypes("""
            package test;

            import dev.langchain4j.agent.tool.Tool;
            import dev.langchain4j.service.Result;
            import io.micronaut.langchain4j.annotation.AiService;
            import java.util.List;
            import java.util.Optional;

            public interface Model {

                @AiService
                interface Assistant {
                    Person person(String text);

                    Result<List<Address>> addresses(String text);

                    Optional<String> text(String text);

                    Hidden hidden(String text);
                }

                record Person(String name, Address address) {
                }

                record Address(String city, Country country) {
                }

                record Country(String name) {
                }

                record Unused(String name) {
                }

                class Plain {
                    private String value;
                }

                @jakarta.inject.Singleton
                class Tools {
                    @Tool("Finds an order")
                    public Order find(OrderQuery query, Plain plain) {
                        return null;
                    }
                }

                record Order(String id) {
                }

                record OrderQuery(String customer) {
                }
            }

            // Micronaut Serialization cannot import a type that is not public
            record Hidden(String name) {
            }
            """);
        assertEquals(Set.of("Person", "Address", "Country", "Order", "OrderQuery"), introspected);
    }

    @Test
    void staticAgentsAndRecordsOfSerdeableRecordsAreImported() {
        Set<String> introspected = introspectedTypes("""
            package test;

            import dev.langchain4j.agentic.Agent;
            import dev.langchain4j.service.V;
            import io.micronaut.serde.annotation.Serdeable;

            public interface Model {

                final class CountryAction {
                    @Agent(outputKey = "country")
                    public static Country country(@V("topic") String topic) {
                        return null;
                    }
                }

                record Country(String name) {
                }

                interface Planner {
                    @Agent(outputKey = "plan")
                    Plan plan(@V("topic") String topic);
                }

                // already @Serdeable: not imported, but the records of its components are
                @Serdeable
                record Plan(Step step) {
                }

                record Step(String description) {
                }
            }
            """);
        // Plan keeps the introspection of its own @Serdeable annotation, which is not an import
        assertEquals(Set.of("Country", "Step"), introspected);
    }

    private static Set<String> introspectedTypes(String source) {
        try (JavaParser parser = new JavaParser()) {
            Iterable<? extends JavaFileObject> generated = parser.generate("test.Model", source);
            return StreamSupport.stream(generated.spliterator(), false)
                .map(file -> INTROSPECTION.matcher(file.getName()))
                .filter(Matcher::matches)
                .map(matcher -> matcher.group(1))
                .collect(Collectors.toSet());
        }
    }
}
