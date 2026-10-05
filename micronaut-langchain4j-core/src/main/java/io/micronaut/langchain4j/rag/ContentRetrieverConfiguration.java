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

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Configures a {@link dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever} that retrieves the
 * segments of an embedding store relevant to the user message. The content retriever bean is named after the
 * configuration, so that the {@link io.micronaut.langchain4j.annotation.AiService} with that name uses it.
 *
 * @since 2.4.0
 */
@EachProperty(ContentRetrieverConfiguration.PREFIX)
public final class ContentRetrieverConfiguration {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.rag.content-retrievers";

    /**
     * The default maximum number of results.
     */
    public static final int DEFAULT_MAX_RESULTS = 3;

    private final String name;
    private @Nullable String embeddingStore;
    private @Nullable String embeddingModel;
    private int maxResults = DEFAULT_MAX_RESULTS;
    private @Nullable Double minScore;

    /**
     * @param name The name of the content retriever
     */
    public ContentRetrieverConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the content retriever
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name of the embedding store bean, or {@code null} for the default embedding store
     */
    public @Nullable String getEmbeddingStore() {
        return embeddingStore;
    }

    /**
     * @param embeddingStore The name of the embedding store bean. Defaults to the default embedding store.
     */
    public void setEmbeddingStore(@Nullable String embeddingStore) {
        this.embeddingStore = embeddingStore;
    }

    /**
     * @return The name of the embedding model bean, or {@code null} for the default embedding model
     */
    public @Nullable String getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * @param embeddingModel The name of the embedding model bean that embeds the user message. Defaults to the default
     *                       embedding model.
     */
    public void setEmbeddingModel(@Nullable String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * @return The maximum number of segments retrieved
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * @param maxResults The maximum number of segments retrieved. Defaults to {@value #DEFAULT_MAX_RESULTS}.
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * @return The minimum relevance score of the segments retrieved
     */
    public @Nullable Double getMinScore() {
        return minScore;
    }

    /**
     * @param minScore The minimum relevance score (between 0 and 1) of the segments retrieved. No minimum by default.
     */
    public void setMinScore(@Nullable Double minScore) {
        this.minScore = minScore;
    }
}
