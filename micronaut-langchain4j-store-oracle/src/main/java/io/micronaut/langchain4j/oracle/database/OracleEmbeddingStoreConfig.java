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
package io.micronaut.langchain4j.oracle.database;

import dev.langchain4j.store.embedding.oracle.EmbeddingTable;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import javax.sql.DataSource;

/**
 * Configures for OracleEmbeddingStore.
 */
@EachProperty(value = OracleEmbeddingStoreConfig.PREFIX, primary = "default")
public class OracleEmbeddingStoreConfig {
    public static final String PREFIX = "langchain4j.oracle.embedding-stores";
    @ConfigurationBuilder(
        prefixes = "",
        excludes = "dataSource"
    )
    final OracleEmbeddingStore.Builder embeddingStoreBuilder;
    @ConfigurationBuilder(
        prefixes = "",
        excludes = "dataSource",
        configurationPrefix = "table"
    )
    final EmbeddingTable.Builder embeddingTable = EmbeddingTable.builder();
    private final DataSource dataSource;

    public OracleEmbeddingStoreConfig(@Parameter DataSource dataSource) {
        this.dataSource = dataSource;
        this.embeddingStoreBuilder = OracleEmbeddingStore.builder()
            .dataSource(dataSource);
    }

    /**
     * The table name
     * @param table The table.
     */
    public void setTable(String table) {
        this.embeddingTable.name(table);
    }

    /**
     * @return The data source.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return The builder
     */
    public OracleEmbeddingStore.Builder getEmbeddingStoreBuilder() {
        embeddingStoreBuilder.embeddingTable(embeddingTable.build());
        return embeddingStoreBuilder;
    }
}
