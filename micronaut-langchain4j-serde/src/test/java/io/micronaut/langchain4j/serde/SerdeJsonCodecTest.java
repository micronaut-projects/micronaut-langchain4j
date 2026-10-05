package io.micronaut.langchain4j.serde;

import dev.langchain4j.exception.JsonReadException;
import dev.langchain4j.internal.Json;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.json.JsonCodecFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.annotation.Serdeable;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SerdeJsonCodecTest {

    private final SerdeJsonCodec codec = new SerdeJsonCodec();

    @Test
    void factoryIsSelectedByLangChain4j() {
        assertInstanceOf(SerdeJsonCodecFactory.class, ServiceHelper.loadFactories(JsonCodecFactory.class).iterator().next());
        // Json goes through the codec: a @Serdeable record is written with its introspection
        assertEquals("{\"name\":\"Fred\",\"age\":42,\"color\":null,\"born\":null}", Json.toJson(new Person("Fred", 42, null, null)));
    }

    @Test
    void serdeableTypesAreMappedWithMicronautSerialization() {
        Person person = codec.fromJson("{\"name\":\"Fred\",\"age\":42,\"color\":\"red\",\"born\":\"2001-02-03\"}", Person.class);
        assertEquals(new Person("Fred", 42, Color.RED, LocalDate.of(2001, 2, 3)), person);
        assertEquals("{\"name\":\"Fred\",\"age\":42,\"color\":\"RED\",\"born\":\"2001-02-03\"}", codec.toJson(person));
        assertFalse(codec.isMappedByJackson(Person.class));
    }

    @Test
    void genericTypes() {
        List<Person> people = codec.fromJson("[{\"name\":\"Fred\",\"age\":42}]", Argument.listOf(Person.class).asType());
        assertEquals(List.of(new Person("Fred", 42, null, null)), people);
        Map<String, Object> arguments = codec.fromJson("{\"city\":\"Paris\",\"days\":3}", Argument.mapOf(String.class, Object.class).asType());
        assertEquals("Paris", arguments.get("city"));
        assertEquals(3, ((Number) arguments.get("days")).intValue());
    }

    @Test
    void unknownPropertiesAreRejected() {
        assertThrows(JsonReadException.class, () -> codec.fromJson("{\"name\":\"Fred\",\"hallucinated\":true}", Person.class));
        assertThrows(JsonReadException.class, () -> codec.fromJson("not json", Person.class));
        assertFalse(codec.isMappedByJackson(Person.class));
    }

    @Test
    void typesWithoutIntrospectionAreMappedWithJackson() {
        Plain plain = codec.fromJson("{\"value\":\"x\"}", Plain.class);
        assertEquals("x", plain.value);
        assertTrue(codec.isMappedByJackson(Plain.class));
        assertEquals("{\"value\":\"x\"}", codec.toJson(plain));
    }

    @Test
    void valuesMicronautSerializationRejectsAreGivenToJackson() {
        // the JSON schema LangChain4j derives for LocalDate describes an object, which the model may follow
        Person person = codec.fromJson("{\"name\":\"Fred\",\"age\":1,\"born\":{\"year\":2001,\"month\":2,\"day\":3}}", Person.class);
        assertEquals(LocalDate.of(2001, 2, 3), person.born());
        assertFalse(codec.isMappedByJackson(Person.class));
        assertNull(codec.fromJson("null", Person.class));
    }

    @Serdeable
    record Person(String name, int age, Color color, LocalDate born) {
    }

    enum Color {
        RED
    }

    static class Plain {
        private String value;
    }
}
