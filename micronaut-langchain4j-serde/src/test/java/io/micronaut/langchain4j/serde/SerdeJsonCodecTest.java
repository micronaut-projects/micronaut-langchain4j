package io.micronaut.langchain4j.serde;

import dev.langchain4j.exception.JsonReadException;
import dev.langchain4j.internal.Json;
import dev.langchain4j.spi.ServiceHelper;
import dev.langchain4j.spi.json.JsonCodecFactory;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.type.Argument;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.serde.config.naming.SnakeCaseStrategy;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SerdeJsonCodecTest {

    private final SerdeJsonCodec codec = new SerdeJsonCodec();

    @Test
    void factoryIsSelectedByLangChain4j() {
        assertInstanceOf(SerdeJsonCodecFactory.class, ServiceHelper.loadFactories(JsonCodecFactory.class).iterator().next());
        // Json goes through the codec: the naming strategy is only known to Micronaut Serialization
        assertEquals("{\"temperature_celsius\":21}", Json.toJson(new Reading(21)));
    }

    @Test
    void serdeableTypesAreMappedWithTheirIntrospection() {
        Person person = codec.fromJson("{\"name\":\"Fred\",\"age\":42,\"color\":\"red\",\"born\":\"2001-02-03\"}", Person.class);
        assertEquals(new Person("Fred", 42, Color.RED, LocalDate.of(2001, 2, 3)), person);
        assertEquals("{\"name\":\"Fred\",\"age\":42,\"color\":\"RED\",\"born\":\"2001-02-03\"}", codec.toJson(person));
        assertEquals(new Reading(21), codec.fromJson("{\"temperature_celsius\":21}", Reading.class));
    }

    @Test
    void genericTypes() {
        List<Person> people = codec.fromJson("[{\"name\":\"Fred\",\"age\":42}]", Argument.listOf(Person.class).asType());
        assertEquals(List.of(new Person("Fred", 42, null, null)), people);
        Map<String, Object> arguments = codec.fromJson("{\"city\":\"Paris\",\"days\":3}", Argument.mapOf(String.class, Object.class).asType());
        assertEquals("Paris", arguments.get("city"));
        assertEquals(3, ((Number) arguments.get("days")).intValue());
        assertNull(codec.fromJson("null", Person.class));
    }

    @Test
    void unknownPropertiesAreRejected() {
        assertThrows(JsonReadException.class, () -> codec.fromJson("{\"name\":\"Fred\",\"hallucinated\":true}", Person.class));
        assertThrows(JsonReadException.class, () -> codec.fromJson("{\"value\":\"x\",\"hallucinated\":true}", Plain.class));
        assertThrows(JsonReadException.class, () -> codec.fromJson("not json", Person.class));
    }

    @Test
    void typesWithoutIntrospectionAreMappedReflectively() {
        // private fields without accessors, like LangChain4j's Jackson codec
        Plain plain = codec.fromJson("{\"value\":\"x\",\"nested\":{\"count\":2}}", Plain.class);
        assertEquals("x", plain.value);
        assertEquals(2, plain.nested.count);
        assertEquals("{\"value\":\"x\",\"nested\":{\"count\":2}}", codec.toJson(plain));

        PlainRecord record = codec.fromJson("{\"name\":\"Fred\",\"born\":{\"year\":2001,\"month\":2,\"day\":3}}", PlainRecord.class);
        assertEquals(new PlainRecord("Fred", LocalDate.of(2001, 2, 3)), record);
    }

    @Test
    void introspectedTypesAreMappedWithTheirIntrospection() {
        IntrospectedBean bean = codec.fromJson("{\"name\":\"Fred\"}", IntrospectedBean.class);
        assertEquals("Fred", bean.getName());
        assertEquals("{\"name\":\"Fred\"}", codec.toJson(bean));
    }

    @Test
    void datesAreAlsoReadInTheShapeOfLangChain4jSchema() {
        // the JSON schema LangChain4j derives for the java.time types describes their fields, which the model may follow
        Dates dates = codec.fromJson("""
            {"date": {"year": 2001, "month": 2, "day": 3},
             "time": {"hour": 4, "minute": 5},
             "dateTime": {"date": {"year": 2001, "month": 2, "day": 3}, "time": {"hour": 4, "minute": 5, "second": 6, "nano": 7}}}""",
            Dates.class);
        assertEquals(new Dates(LocalDate.of(2001, 2, 3), LocalTime.of(4, 5), LocalDateTime.of(2001, 2, 3, 4, 5, 6, 7)), dates);
        assertEquals(new Dates(LocalDate.of(2001, 2, 3), LocalTime.of(4, 5), LocalDateTime.of(2001, 2, 3, 4, 5, 6, 7)),
            codec.fromJson("{\"date\":\"2001-02-03\",\"time\":\"04:05\",\"dateTime\":\"2001-02-03T04:05:06.000000007\"}", Dates.class));
        assertEquals("{\"date\":\"2001-02-03\",\"time\":\"04:05:00\",\"dateTime\":\"2001-02-03T04:05:06.000000007\"}",
            codec.toJson(new Dates(LocalDate.of(2001, 2, 3), LocalTime.of(4, 5), LocalDateTime.of(2001, 2, 3, 4, 5, 6, 7))));
    }

    @Test
    void theApplicationMapperIsNotChanged() {
        try (ApplicationContext context = ApplicationContext.run()) {
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            assertThrows(IOException.class, () -> mapper.readValue("{\"year\":2001,\"month\":2,\"day\":3}", LocalDate.class));
        }
    }

    @Serdeable
    record Person(String name, int age, Color color, LocalDate born) {
    }

    @Serdeable(naming = SnakeCaseStrategy.class)
    record Reading(int temperatureCelsius) {
    }

    @Serdeable
    record Dates(LocalDate date, LocalTime time, LocalDateTime dateTime) {
    }

    enum Color {
        RED
    }

    static class Plain {
        private String value;
        private Nested nested;
    }

    static class Nested {
        private int count;
    }

    record PlainRecord(String name, LocalDate born) {
    }

    @Introspected
    public static class IntrospectedBean {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
