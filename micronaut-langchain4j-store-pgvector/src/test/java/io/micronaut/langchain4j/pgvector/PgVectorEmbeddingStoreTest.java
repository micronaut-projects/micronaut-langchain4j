package io.micronaut.langchain4j.pgvector;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false)
public class PgVectorEmbeddingStoreTest {
    @Test
    void testEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
        Assertions.assertNotNull(embeddingStore);
        Assertions.assertInstanceOf(PgVectorEmbeddingStore.class, embeddingStore);
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        TextSegment segment1 = TextSegment.from("I like football.");
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        embeddingStore.add(embedding1, segment1);

        TextSegment segment2 = TextSegment.from("The weather is good today.");
        Embedding embedding2 = embeddingModel.embed(segment2).content();
        embeddingStore.add(embedding2, segment2);

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
        EmbeddingSearchResult<TextSegment> relevant = embeddingStore.search(EmbeddingSearchRequest.builder().queryEmbedding(queryEmbedding).maxResults(1).build());
        EmbeddingMatch<TextSegment> embeddingMatch = relevant.matches().get(0);

        System.out.println(embeddingMatch.score()); // 0.8144288608390052
        Assertions.assertEquals("I like football.", embeddingMatch.embedded().text());
    }
}
