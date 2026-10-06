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

import org.jspecify.annotations.Nullable;

/**
 * The name, embedding store and embedding model shared by the retrieval augmented generation configurations.
 *
 * @since 2.4.0
 */
abstract sealed class EmbeddingStoreUsingConfiguration permits ContentRetrieverConfiguration, IngestionConfiguration {

    private final String name;
    private @Nullable String embeddingStore;
    private @Nullable String embeddingModel;

    EmbeddingStoreUsingConfiguration(String name) {
        this.name = name;
    }

    /**
     * @return The name of the configuration, which is the name of the bean it configures
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
     * @param embeddingModel The name of the embedding model bean. Defaults to the default embedding model.
     */
    public void setEmbeddingModel(@Nullable String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }
}
