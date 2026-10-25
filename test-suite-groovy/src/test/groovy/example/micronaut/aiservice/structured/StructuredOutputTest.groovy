package example.micronaut.aiservice.structured

import example.structured.TalkRecommendation
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import dev.langchain4j.model.chat.request.json.JsonSchema
import io.micronaut.langchain4j.jsonschema.StructuredOutputSchemaProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

@MicronautTest(startApplication = false, environments = "structured-output")
class StructuredOutputTest {

    @Inject
    TalkAdvisor talkAdvisor

    @Inject
    StructuredOutputChatModel chatModel

    @Inject
    StructuredOutputSchemaProvider schemaProvider

    @Test
    void sendsTheGeneratedSchema() {
        TalkRecommendation recommendation = talkAdvisor.recommend("LLMs and Java") // <1>

        assertEquals(new TalkRecommendation(title: "Structured outputs", reason: "You like LLMs", score: 5), recommendation)
        JsonSchema schema = chatModel.lastRequest.responseFormat().jsonSchema() // <2>
        assertEquals("TalkRecommendation", schema.name())
        assertEquals(schemaProvider.findSchema(TalkRecommendation).orElseThrow(), schema.rootElement()) // <3>
        JsonObjectSchema root = (JsonObjectSchema) schema.rootElement()
        assertEquals("A conference talk recommended to an attendee.", root.description())
        assertEquals(["title", "reason", "score"] as Set, root.properties().keySet())
    }

    @Test
    void sendsTheGeneratedSchemaOfListElements() {
        List<TalkRecommendation> shortlist = talkAdvisor.shortlist("LLMs and Java")

        assertEquals(1, shortlist.size())
        JsonSchema schema = chatModel.lastRequest.responseFormat().jsonSchema()
        assertEquals("List_of_TalkRecommendation", schema.name())
        JsonArraySchema values = (JsonArraySchema) ((JsonObjectSchema) schema.rootElement()).properties().get("values")
        assertEquals(schemaProvider.findSchema(TalkRecommendation).orElseThrow(), values.items())
    }
}
