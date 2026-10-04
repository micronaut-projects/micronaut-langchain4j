/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.infinispan;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.infinispan.InfinispanEmbeddingStore;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@MicronautTest(transactional = false)
class InfinispanEmbeddingStoreTest {
    @Test
    void testEmbeddingStore(EmbeddingStore<TextSegment> embeddingStore) {
        Assertions.assertNotNull(embeddingStore);
        Assertions.assertInstanceOf(InfinispanEmbeddingStore.class, embeddingStore);
        embeddingStore.removeAll();

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        TextSegment football = TextSegment.from("I like football.", Metadata.from(Map.of("category", "sport")));
        TextSegment weather = TextSegment.from("The weather is good today.", Metadata.from(Map.of("category", "weather")));
        Embedding footballEmbedding = embeddingModel.embed(football).content();
        Embedding weatherEmbedding = embeddingModel.embed(weather).content();
        String footballId = embeddingStore.add(footballEmbedding, football);
        embeddingStore.add(weatherEmbedding, weather);

        Embedding queryEmbedding = embeddingModel.embed("What is your favourite sport?").content();
        var filteredResult = embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(2)
            .filter(MetadataFilterBuilder.metadataKey("category").isEqualTo("sport"))
            .build());

        Assertions.assertEquals(1, filteredResult.matches().size());
        Assertions.assertEquals("I like football.", filteredResult.matches().getFirst().embedded().text());

        embeddingStore.removeAll(List.of(footballId));
        var remainingSports = embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(2)
            .filter(MetadataFilterBuilder.metadataKey("category").isEqualTo("sport"))
            .build());
        Assertions.assertTrue(remainingSports.matches().isEmpty());

        embeddingStore.removeAll();
        var allResults = embeddingStore.search(EmbeddingSearchRequest.builder()
            .queryEmbedding(queryEmbedding)
            .maxResults(2)
            .build());
        Assertions.assertTrue(allResults.matches().isEmpty());
    }
}
