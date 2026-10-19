package io.micronaut.langchain4j.milvus;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.v2.MilvusV2EmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.milvus.MilvusContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Testcontainers(disabledWithoutDocker = true)
class MilvusEmbeddingStoreTest {

    @Container
    static final MilvusContainer CONTAINER = new MilvusContainer("milvusdb/milvus:v2.5.10");

    @Test
    void addsAndSearchesEmbeddings() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.milvus-v2.embedding-store.uri", CONTAINER.getEndpoint(),
            "langchain4j.milvus-v2.embedding-store.collection-name", "documents",
            "langchain4j.milvus-v2.embedding-store.dimension", 3,
            "langchain4j.milvus-v2.embedding-store.consistency-level", "STRONG"
        ))) {
            EmbeddingStore<TextSegment> store = context.getBean(Argument.of(EmbeddingStore.class, TextSegment.class));
            assertInstanceOf(MilvusV2EmbeddingStore.class, store);

            store.add(Embedding.from(new float[] {1f, 0f, 0f}), TextSegment.from("Micronaut"));
            store.add(Embedding.from(new float[] {0f, 1f, 0f}), TextSegment.from("Other"));

            List<EmbeddingMatch<TextSegment>> matches = store.search(EmbeddingSearchRequest.builder()
                .queryEmbedding(Embedding.from(new float[] {0.9f, 0.1f, 0f}))
                .maxResults(1)
                .build()).matches();
            assertEquals("Micronaut", matches.getFirst().embedded().text());
        }
    }
}
