from dev.langchain4j.data.segment import TextSegment
from dev.langchain4j.model.embedding import EmbeddingModel
from dev.langchain4j.rag.content.retriever import ContentRetriever, EmbeddingStoreContentRetriever
from dev.langchain4j.store.embedding import EmbeddingStore
from jakarta.inject import Singleton
from micronaut.context.annotation import Factory, Requires


@Requires(property="spec.name", value="RagTest")
@Factory
class RetrievalFactory:

    @Singleton  # <1>
    def contentRetriever(self, embedding_store: EmbeddingStore[TextSegment], embedding_model: EmbeddingModel) -> ContentRetriever:  # <2>
        return EmbeddingStoreContentRetriever.builder() \
            .embeddingStore(embedding_store) \
            .embeddingModel(embedding_model) \
            .maxResults(3) \
            .build()
