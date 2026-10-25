from typing import Annotated

from jakarta.inject import Inject
from micronaut.langchain4j.jsonschema import StructuredOutputSchemaProvider
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.agentic.TalkPickerAgent import TalkPickerAgent
from example.micronaut.aiservice.structured.StructuredOutputChatModel import StructuredOutputChatModel
from example.structured.TalkRecommendation import TalkRecommendation


@MicronautTest(startApplication=False, environments=["structured-output"])
class StructuredOutputAgentTest:
    agent: Annotated[TalkPickerAgent, Inject]
    chat_model: Annotated[StructuredOutputChatModel, Inject]
    schema_provider: Annotated[StructuredOutputSchemaProvider, Inject]

    @Test
    def test_agent_sends_the_generated_schema(self):
        recommendation = self.agent.pick("LLMs and Java")

        assert recommendation.title == "Structured outputs"
        schema = self.chat_model.last_request().responseFormat().jsonSchema()
        assert schema.name() == "TalkRecommendation"
        assert schema.rootElement().equals(self.schema_provider.findSchema(TalkRecommendation).orElseThrow())
        assert schema.rootElement().properties().get("reason").description() == \
            "Why the talk matches the interests of the attendee"
