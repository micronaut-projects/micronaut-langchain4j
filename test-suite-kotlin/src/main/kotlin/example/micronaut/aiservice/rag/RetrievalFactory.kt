package example.micronaut.aiservice.rag

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.rag.content.retriever.ContentRetriever
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.store.embedding.EmbeddingStore
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Requires(property = "spec.name", value = "RagTest")
@Factory
internal class RetrievalFactory {

    @Singleton // <1>
    fun contentRetriever(embeddingStore: EmbeddingStore<TextSegment>, embeddingModel: EmbeddingModel): ContentRetriever = // <2>
        EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(3)
            .build()
}
