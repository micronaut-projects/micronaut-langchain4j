/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.mongodb.atlas;

import com.mongodb.client.MongoClient;
import com.mongodb.client.model.CreateCollectionOptions;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import java.util.Set;

/**
 * Configures for PgVectorEmbeddingStore.
 */
@EachProperty(value = NamedMongoDbAtlasEmbeddingStoreConfig.PREFIX, primary = "default")
public class NamedMongoDbAtlasEmbeddingStoreConfig {
    public static final String PREFIX = "langchain4j.mongodb-atlas.embedding-stores";
    @ConfigurationBuilder(
        prefixes = "",
        excludes = { "fromClient", "mongoClient", "filter", "createCollectionOptions", "indexMapping"}
    )
    final MongoDbEmbeddingStore.Builder builder;
    private final MongoClient mongoClient;

    public NamedMongoDbAtlasEmbeddingStoreConfig(
        @Parameter MongoClient mongoClient,
        @Parameter @Nullable IndexMappingConfig indexMappingConfig,
        @Parameter @Nullable CreateCollectionOptions createCollectionOptions) {
        this.mongoClient = mongoClient;
        this.builder = MongoDbEmbeddingStore.builder()
            .fromClient(mongoClient)
            .indexMapping(indexMappingConfig != null ? indexMappingConfig.indexMapping.build() : null)
            .createCollectionOptions(createCollectionOptions);
    }

    /**
     * @return The data source.
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * @return The embedded store builder.
     */
    public MongoDbEmbeddingStore.Builder getBuilder() {
        return builder;
    }

    /**
     * index mapping configuration.
     */
    @ConfigurationProperties("index-mapping")
    static class IndexMappingConfig {
        @ConfigurationBuilder(prefixes = "")
        final IndexMapping.Builder indexMapping;

        public IndexMappingConfig() {
            this.indexMapping = IndexMapping.builder().dimension(1536).metadataFieldNames(Set.of());
        }
    }
}
