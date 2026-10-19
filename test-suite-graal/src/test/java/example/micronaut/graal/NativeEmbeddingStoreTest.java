package example.micronaut.graal;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The default in-memory embedding store in a native image.
 */
@MicronautTest(startApplication = false)
class NativeEmbeddingStoreTest {

    @Inject
    EmbeddingStore<TextSegment> embeddingStore;

    @Test
    void addsAndSearchesEmbeddings() {
        embeddingStore.add(Embedding.from(new float[] {1f, 0f}), TextSegment.from("Micronaut"));
        embeddingStore.add(Embedding.from(new float[] {0f, 1f}), TextSegment.from("Other"));

        var matches = embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(Embedding.from(new float[] {0.9f, 0.1f}))
            .maxResults(1)
            .build()).matches();

        assertEquals("Micronaut", matches.getFirst().embedded().text());
    }
}
