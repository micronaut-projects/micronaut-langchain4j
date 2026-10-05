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
package io.micronaut.langchain4j.rag;

import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.jspecify.annotations.Nullable;

/**
 * Creates the content retrievers and the ingestors of the RAG configuration.
 */
@Factory
@Internal
final class RagFactory {

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Argument<EmbeddingStore<TextSegment>> EMBEDDING_STORE = (Argument) Argument.of(EmbeddingStore.class, TextSegment.class);

    private final BeanContext beanContext;

    RagFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @EachBean(ContentRetrieverConfiguration.class)
    ContentRetriever contentRetriever(ContentRetrieverConfiguration configuration) {
        EmbeddingStoreContentRetriever.EmbeddingStoreContentRetrieverBuilder builder = EmbeddingStoreContentRetriever.builder()
            .displayName(configuration.getName())
            .embeddingStore(bean(EMBEDDING_STORE, configuration.getEmbeddingStore()))
            .embeddingModel(bean(Argument.of(EmbeddingModel.class), configuration.getEmbeddingModel()))
            .maxResults(configuration.getMaxResults());
        if (configuration.getMinScore() != null) {
            builder.minScore(configuration.getMinScore());
        }
        return builder.build();
    }

    @EachBean(IngestionConfiguration.class)
    EmbeddingStoreIngestor embeddingStoreIngestor(IngestionConfiguration configuration) {
        return EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(configuration.getMaxSegmentSize(), configuration.getMaxOverlapSize()))
            .embeddingStore(bean(EMBEDDING_STORE, configuration.getEmbeddingStore()))
            .embeddingModel(bean(Argument.of(EmbeddingModel.class), configuration.getEmbeddingModel()))
            .build();
    }

    private <T> T bean(Argument<T> type, @Nullable String name) {
        return name == null ? beanContext.getBean(type) : beanContext.getBean(type, Qualifiers.byName(name));
    }
}
