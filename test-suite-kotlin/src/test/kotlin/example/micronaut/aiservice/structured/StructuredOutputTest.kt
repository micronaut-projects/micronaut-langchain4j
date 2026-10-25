package example.micronaut.aiservice.structured

import example.structured.TalkRecommendation
import dev.langchain4j.model.chat.request.json.JsonArraySchema
import dev.langchain4j.model.chat.request.json.JsonObjectSchema
import io.micronaut.langchain4j.jsonschema.StructuredOutputSchemaProvider
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false, environments = ["structured-output"])
class StructuredOutputTest {

    @Inject
    lateinit var talkAdvisor: TalkAdvisor

    @Inject
    lateinit var chatModel: StructuredOutputChatModel

    @Inject
    lateinit var schemaProvider: StructuredOutputSchemaProvider

    @Test
    fun sendsTheGeneratedSchema() {
        val recommendation = talkAdvisor.recommend("LLMs and Java") // <1>

        assertEquals(TalkRecommendation("Structured outputs", "You like LLMs", 5), recommendation)
        val schema = chatModel.lastRequest!!.responseFormat().jsonSchema() // <2>
        assertEquals("TalkRecommendation", schema.name())
        assertEquals(schemaProvider.findSchema(TalkRecommendation::class.java).orElseThrow(), schema.rootElement()) // <3>
        val root = schema.rootElement() as JsonObjectSchema
        assertEquals("A conference talk recommended to an attendee.", root.description())
        assertEquals(setOf("title", "reason", "score"), root.properties().keys)
        assertEquals("Why the talk matches the interests of the attendee", root.properties()["reason"]!!.description())
    }

    @Test
    fun sendsTheGeneratedSchemaOfListElements() {
        val shortlist = talkAdvisor.shortlist("LLMs and Java")

        assertEquals(1, shortlist.size)
        val schema = chatModel.lastRequest!!.responseFormat().jsonSchema()
        assertEquals("List_of_TalkRecommendation", schema.name())
        val values = (schema.rootElement() as JsonObjectSchema).properties()["values"] as JsonArraySchema
        assertEquals(schemaProvider.findSchema(TalkRecommendation::class.java).orElseThrow(), values.items())
    }
}
