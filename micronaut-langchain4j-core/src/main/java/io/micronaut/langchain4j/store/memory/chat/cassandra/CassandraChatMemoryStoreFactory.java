/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.langchain4j.store.memory.chat.cassandra;

import dev.langchain4j.store.memory.chat.cassandra.CassandraChatMemoryStore;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
@Internal
class CassandraChatMemoryStoreFactory {
    @Named("cassandra")
    @Prototype
    CassandraChatMemoryStore.Builder createCassandraChatMemoryStoreBuilder(CassandraChatMemoryStoreConfiguration config) {
        CassandraChatMemoryStore.Builder builder = CassandraChatMemoryStore.builder();
        if (CollectionUtils.isNotEmpty(config.getContactPoints())) {
            builder.contactPoints(config.getContactPoints());
        }
        if (StringUtils.isNotEmpty(config.getLocalDataCenter())) {
            builder.localDataCenter(config.getLocalDataCenter());
        }
        if (StringUtils.isNotEmpty(config.getUserName())) {
            builder.userName(config.getUserName());
        }
        if (StringUtils.isNotEmpty(config.getPassword())) {
            builder.password(config.getPassword());
        }
        if (StringUtils.isNotEmpty(config.getKeyspace())) {
            builder.keyspace(config.getKeyspace());
        }
        if (StringUtils.isNotEmpty(config.getTable())) {
            builder.table(config.getTable());
        }
        if (config.getPort() != null) {
            builder.port(config.getPort());
        }
        return builder;
    }

    @Singleton
    @EachBean(CassandraChatMemoryStore.Builder.class)
    CassandraChatMemoryStore createCassandraChatMemoryStore(CassandraChatMemoryStore.Builder builder) {
        CassandraChatMemoryStore memoryStore = builder.build();
        memoryStore.create(); // Create the table if not exist.
        return memoryStore;
    }
}
