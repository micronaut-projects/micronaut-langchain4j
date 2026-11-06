from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.tutorial.KnowledgeAssistant import KnowledgeAssistant


@Property(name="spec.name", value="KnowledgeAssistantTest")
@Property(name="langchain4j.rag.ingestion.knowledge.sources", value="classpath:tutorial-docs")  # <1>
@Property(name="langchain4j.rag.content-retrievers.knowledge.max-results", value="1")  # <2>
@MicronautTest(startApplication=False, environments=["scripted-test"])
class KnowledgeAssistantTest:
    assistant: Annotated[KnowledgeAssistant, Inject]

    @Test
    def test_answers_from_the_documents_and_the_tools(self):
        result = self.assistant.ask("What is the latest release?")  # <3>

        assert any("GraalVM" in source.textSegment().text() for source in result.sources())  # <4>
        assert result.content() == "tool:2.4.0"  # <5>
