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

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.oracle.OracleEmbeddingStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;

/**
 * Factory for PgVector embedding store.
 */
@Factory
public class OracleEmbeddingStoreFactory {

    /**
     * Creates the oracle embedding store.
     * @param configuration The configuration
     * @return The store
     */
    @EachBean(OracleEmbeddingStoreConfig.class)
    @Context
    @Bean(typed = EmbeddingStore.class)
    protected OracleEmbeddingStore oracleEmbeddingStore(OracleEmbeddingStoreConfig configuration) {
        return configuration.getEmbeddingStoreBuilder()
            .build();
    }
}
