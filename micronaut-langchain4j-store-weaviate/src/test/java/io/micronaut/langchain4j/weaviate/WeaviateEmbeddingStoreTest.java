package io.micronaut.langchain4j.weaviate;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.type.Argument;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.weaviate.WeaviateContainer;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Testcontainers(disabledWithoutDocker = true)
class WeaviateEmbeddingStoreTest {

    @Container
    static final WeaviateContainer CONTAINER = new WeaviateContainer("semitechnologies/weaviate:1.32.4");

    @Test
    void addsAndSearchesEmbeddings() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.weaviate.embedding-store.scheme", "http",
            "langchain4j.weaviate.embedding-store.host", CONTAINER.getHttpHostAddress(),
            "langchain4j.weaviate.embedding-store.object-class", "Document"
        ))) {
            EmbeddingStore<TextSegment> store = context.getBean(Argument.of(EmbeddingStore.class, TextSegment.class));
            assertInstanceOf(WeaviateEmbeddingStore.class, store);

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
