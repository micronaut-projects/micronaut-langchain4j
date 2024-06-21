package io.micronaut.langchain4j.embedding;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false, startApplication = false)
class InMemoryStoreTest {
    @Inject EmbeddingStore<TextSegment> store;
    @Test
    void testDefault() {
        assertNotNull(store);
    }
}
