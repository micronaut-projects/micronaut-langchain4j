from typing import Annotated

from dev.langchain4j.data.segment import TextSegment
from dev.langchain4j.model.embedding import EmbeddingModel
from dev.langchain4j.store.embedding import EmbeddingStore
from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.rag.Expert import Expert


@Property(name="spec.name", value="RagTest")
@MicronautTest(startApplication=False, environments=["scripted-test"])
class RagTest:
    expert: Annotated[Expert, Inject]
    embedding_store: Annotated[EmbeddingStore[TextSegment], Inject]
    embedding_model: Annotated[EmbeddingModel, Inject]

    @Test
    def test_retrieved_content_is_passed_to_the_model(self):
        fact = TextSegment.from_("Micronaut LangChain4j integrates LangChain4j with the Micronaut framework.")
        self.embedding_store.add(self.embedding_model.embed(fact).content(), fact)  # <1>

        result = self.expert.ask("What does Micronaut LangChain4j do?")  # <2>

        assert result.content() is not None
        assert any(source.textSegment().text() == fact.text() for source in result.sources())  # <3>
