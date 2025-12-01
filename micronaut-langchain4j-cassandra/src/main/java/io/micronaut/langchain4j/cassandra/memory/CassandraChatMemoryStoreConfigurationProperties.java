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
package io.micronaut.langchain4j.cassandra.memory;

import dev.langchain4j.store.memory.chat.cassandra.CassandraChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.NonNull;

/**
 * {@link ConfigurationProperties} implementation for {@link CassandraChatMemoryStoreConfiguration}.
 */
@Internal
@ConfigurationProperties(CassandraChatMemoryStoreConfiguration.PREFIX)
class CassandraChatMemoryStoreConfigurationProperties implements CassandraChatMemoryStoreConfiguration {
    private boolean enabled = DEFAULT_ENABLED;

    @ConfigurationBuilder(prefixes = "")
    private CassandraChatMemoryStore.Builder builder = CassandraChatMemoryStore.builder();

    /**
     * Whether Neo4j ChatMemory store is enabled. Default value true
     * @return enabled Whether Neo4j ChatMemory store is enabled. Default value true
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled Whether Neo4j ChatMemory store is enabled. Default value true
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    @NonNull
    public CassandraChatMemoryStore.Builder getBuilder() {
        return builder;
    }

    public void setBuilder(@NonNull CassandraChatMemoryStore.Builder builder) {
        this.builder = builder;
    }
}
