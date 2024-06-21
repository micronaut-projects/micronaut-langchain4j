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
package io.micronaut.langchain4j.pgvector;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import javax.sql.DataSource;

/**
 * Factory for PgVector embedding store.
 */
@Factory
public class PgVectorEmbeddingStoreFactory {
    @EachBean(PgVectorEmbeddingStoreConfig.class)
    @Context
    @Bean(typed = EmbeddingStore.class)
    protected PgVectorEmbeddingStore pgVectorEmbeddingStore(PgVectorEmbeddingStoreConfig configuration) {
        return new DataSourcePgVectorStore(
                configuration.getDataSource(),
                configuration.getTable(),
                configuration.getDimension(),
                configuration.getUseIndex(),
                configuration.getIndexListSize(),
                configuration.getCreateTable(),
                configuration.getDropTableFirst(),
                configuration.getMetadataStorageConfig()
        );
    }

    static final class DataSourcePgVectorStore extends PgVectorEmbeddingStore {
        DataSourcePgVectorStore(DataSource datasource, String table, Integer dimension, Boolean useIndex, Integer indexListSize, Boolean createTable, Boolean dropTableFirst, MetadataStorageConfig metadataStorageConfig) {
            super(datasource, table, dimension, useIndex, indexListSize, createTable, dropTableFirst, metadataStorageConfig);
        }
    }
}
