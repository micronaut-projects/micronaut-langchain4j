from typing import Annotated

from micronaut.langchain4j.jsonschema import StructuredOutputSchemaProvider
from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.structured.StructuredOutputChatModel import StructuredOutputChatModel
from example.micronaut.aiservice.structured.TalkAdvisor import TalkAdvisor
from example.structured.TalkRecommendation import TalkRecommendation


@MicronautTest(startApplication=False, environments=["structured-output"])
class StructuredOutputTest:
    talk_advisor: Annotated[TalkAdvisor, Inject]
    chat_model: Annotated[StructuredOutputChatModel, Inject]
    schema_provider: Annotated[StructuredOutputSchemaProvider, Inject]

    @Test
    def test_sends_the_generated_schema(self):
        recommendation = self.talk_advisor.recommend("LLMs and Java")  # <1>

        assert recommendation.title == "Structured outputs"
        assert recommendation.score == 5
        schema = self.chat_model.last_request().responseFormat().jsonSchema()  # <2>
        assert schema.name() == "TalkRecommendation"
        assert schema.rootElement().equals(self.schema_provider.findSchema(TalkRecommendation).orElseThrow())  # <3>
        root = schema.rootElement()
        assert root.description() == "A conference talk recommended to an attendee."
        assert set(str(name) for name in root.properties().keySet()) == {"title", "reason", "score"}

    @Test
    def test_sends_the_generated_schema_of_list_elements(self):
        shortlist = self.talk_advisor.shortlist("LLMs and Java")

        assert len(shortlist) == 1
        schema = self.chat_model.last_request().responseFormat().jsonSchema()
        assert schema.name() == "List_of_TalkRecommendation"
        values = schema.rootElement().properties().get("values")
        assert values.items().equals(self.schema_provider.findSchema(TalkRecommendation).orElseThrow())
