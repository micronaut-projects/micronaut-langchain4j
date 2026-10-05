package example.micronaut.graal;

import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structured outputs work in a native image: the records are mapped with Micronaut Serialization
 * (micronaut-langchain4j-serde) and their JSON schema is derived by LangChain4j with the reflection metadata generated
 * by micronaut-langchain4j-processor.
 */
@MicronautTest(startApplication = false)
public class NativeStructuredOutputTest {

    @Inject
    Forecaster forecaster;

    @Test
    void recordsAreImportedIntoMicronautSerialization() {
        for (Class<?> type : List.of(Forecast.class, Location.class)) {
            assertTrue(BeanIntrospector.SHARED.findIntrospection(type).isPresent(), type.getName());
        }
    }

    @Test
    void structuredOutput() {
        assertEquals(
            new Forecast(new Location("Paris"), 21, Conditions.SUNNY, List.of("light wind")),
            forecaster.forecast(EchoChatModel.JSON
                + "{\"location\": {\"city\": \"Paris\"}, \"temperature\": 21, \"conditions\": \"sunny\", \"notes\": [\"light wind\"]}"));
    }

    @Test
    void listOfStructuredOutputs() {
        assertEquals(
            List.of(new Location("Paris"), new Location("Lyon")),
            // LangChain4j wraps the items of a list in the "values" property of its JSON schema
            forecaster.locations(EchoChatModel.JSON + "{\"values\": [{\"city\": \"Paris\"}, {\"city\": \"Lyon\"}]}"));
    }

    @Test
    void serdeableRecordsAreMappedWithMicronautSerialization() {
        // the naming strategy is only known to Micronaut Serialization: Jackson would reject the property
        assertEquals(new Reading(21), forecaster.reading(EchoChatModel.JSON + "{\"temperature_celsius\": 21}"));
    }

    @AiService
    public interface Forecaster {
        Forecast forecast(String request);

        Reading reading(String request);

        List<Location> locations(String request);
    }

    public record Location(String city) {
    }

    public enum Conditions {
        SUNNY, CLOUDY
    }

    @Serdeable(naming = SnakeCaseStrategy.class)
    public record Reading(int temperatureCelsius) {
    }

    public record Forecast(Location location, int temperature, Conditions conditions, List<String> notes) {
    }
}
