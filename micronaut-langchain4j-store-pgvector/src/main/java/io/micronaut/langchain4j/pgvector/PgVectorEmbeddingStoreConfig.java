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

import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;
import javax.sql.DataSource;

/**
 * Configures for PgVectorEmbeddingStore.
 */
@EachProperty(value = PgVectorEmbeddingStoreConfig.PREFIX, primary = "default")
public class PgVectorEmbeddingStoreConfig {
    public static final String PREFIX = "langchain4j.pgvector.embedding-stores";
    private final DataSource dataSource;
    private String table;
    private Integer dimension;
    private Boolean useIndex;
    private Integer indexListSize;
    private Boolean createTable;
    private Boolean dropTableFirst;
    private MetadataStorageConfig metadataStorageConfig;

    public PgVectorEmbeddingStoreConfig(
        @Parameter DataSource dataSource,
        @Parameter @Nullable MetadataStorageConfig metadataStorageConfig) {
        this.dataSource = dataSource;
        this.metadataStorageConfig = metadataStorageConfig;
    }

    /**
     * @return The data source.
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return The table.
     */
    public String getTable() {
        return table;
    }

    /**
     * The table to use.
     * @param table The table.
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @return The dimension to use.
     */
    public Integer getDimension() {
        return dimension;
    }

    /**
     * Sets the dimension.
     * @param dimension The dimension
     */
    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    /**
     * Whether to use the index.
     * @return True if the index should be used
     */
    public Boolean getUseIndex() {
        return useIndex;
    }

    /**
     * Sets whether to use the index.
     * @param useIndex True if the index should be used
     */
    public void setUseIndex(Boolean useIndex) {
        this.useIndex = useIndex;
    }

    /**
     * The index list size.
     * @return The index list size.
     */
    public Integer getIndexListSize() {
        return indexListSize;
    }

    /**
     * Sets the index list size.
     * @param indexListSize The index list size.
     */
    public void setIndexListSize(Integer indexListSize) {
        this.indexListSize = indexListSize;
    }

    /**
     * Whether to create the table.
     * @return Whether to create the table
     */
    public Boolean getCreateTable() {
        return createTable;
    }

    /**
     * Sets whether to create the table.
     * @param createTable The table
     */
    public void setCreateTable(Boolean createTable) {
        this.createTable = createTable;
    }

    /**
     * Whether to drop the table first.
     * @return True if the table should be dropped.
     */
    public Boolean getDropTableFirst() {
        return dropTableFirst;
    }

    /**
     * Sets whether the table should be dropped.
     * @param dropTableFirst True if the table should be dropped.
     */
    public void setDropTableFirst(Boolean dropTableFirst) {
        this.dropTableFirst = dropTableFirst;
    }

    /**
     * The metadata storage config.
     * @return The config
     */
    public MetadataStorageConfig getMetadataStorageConfig() {
        return metadataStorageConfig;
    }
}
