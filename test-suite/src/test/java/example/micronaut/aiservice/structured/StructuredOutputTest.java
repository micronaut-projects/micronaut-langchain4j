package example.micronaut.aiservice.structured;

import dev.langchain4j.model.chat.request.json.JsonArraySchema;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.request.json.JsonSchema;
import example.structured.TalkRecommendation;
import io.micronaut.langchain4j.jsonschema.StructuredOutputSchemaProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false, environments = "structured-output")
class StructuredOutputTest {

    @Inject
    TalkAdvisor talkAdvisor;

    @Inject
    StructuredOutputChatModel chatModel;

    @Inject
    StructuredOutputSchemaProvider schemaProvider;

    @Test
    void sendsTheGeneratedSchema() {
        TalkRecommendation recommendation = talkAdvisor.recommend("LLMs and Java"); // <1>

        assertEquals(new TalkRecommendation("Structured outputs", "You like LLMs", 5), recommendation);
        JsonSchema schema = chatModel.lastRequest().responseFormat().jsonSchema(); // <2>
        assertEquals("TalkRecommendation", schema.name());
        assertEquals(schemaProvider.findSchema(TalkRecommendation.class).orElseThrow(), schema.rootElement()); // <3>
        JsonObjectSchema root = (JsonObjectSchema) schema.rootElement();
        assertEquals("A conference talk recommended to an attendee.", root.description());
        assertEquals(Set.of("title", "reason", "score"), root.properties().keySet());
        assertEquals("Why the talk matches the interests of the attendee", root.properties().get("reason").description());
    }

    @Test
    void sendsTheGeneratedSchemaOfListElements() {
        List<TalkRecommendation> shortlist = talkAdvisor.shortlist("LLMs and Java");

        assertEquals(1, shortlist.size());
        JsonSchema schema = chatModel.lastRequest().responseFormat().jsonSchema();
        assertEquals("List_of_TalkRecommendation", schema.name());
        JsonArraySchema values = (JsonArraySchema) ((JsonObjectSchema) schema.rootElement()).properties().get("values");
        assertEquals(schemaProvider.findSchema(TalkRecommendation.class).orElseThrow(), values.items());
    }
}
