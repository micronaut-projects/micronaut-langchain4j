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
import org.jspecify.annotations.NonNull;
import io.micronaut.core.util.Toggleable;

/**
 * Configuration for {@link dev.langchain4j.store.memory.chat.cassandra.CassandraChatMemoryStore.Builder}.
 */
public interface CassandraChatMemoryStoreConfiguration extends Toggleable {
    /**
     * CassandraChatMemoryStore configuration prefix.
     */
    String PREFIX = "langchain4j.chat-memory-store.cassandra";
    /**
     * CassandraChatMemoryStore default value for enabled.
     */
    boolean DEFAULT_ENABLED = true;
    /**
     * CassandraChatMemoryStore enabled configuration prefix.
     */
    String PROPERTY_ENABLED = PREFIX + ".enabled";

    /**
     *
     * @return Whether Neo4j ChatMemory store is enabled.
     */
    @Override
    default boolean isEnabled() {
        return DEFAULT_ENABLED;
    }

    /**
     *
     * @return Cassandra Chat Memory Store builder
     */
    @NonNull
    CassandraChatMemoryStore.Builder getBuilder();
}
