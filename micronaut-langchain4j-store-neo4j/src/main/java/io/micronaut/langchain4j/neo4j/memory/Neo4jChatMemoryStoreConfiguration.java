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
package io.micronaut.langchain4j.neo4j.memory;

import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;

/**
 * Configuration for {@link dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore.Builder}.
 */
public interface Neo4jChatMemoryStoreConfiguration extends Toggleable {
    /**
     * Neo4jChatMemoryStore configuration prefix.
     */
    String PREFIX = "langchain4j.chat-memory-store.neo4j";
    /**
     * Neo4jChatMemoryStore default value for enabled.
     */
    boolean DEFAULT_ENABLED = true;
    /**
     * Neo4jChatMemoryStore enabled configuration prefix.
     */
    String PROPERTY_ENABLED = PREFIX + ".enabled";

    @Override
    default boolean isEnabled() {
        return DEFAULT_ENABLED;
    }

    /**
     *
     * @return Neo4j Chat Memory Store Builder
     */
    @NonNull
    Neo4jChatMemoryStore.Builder getBuilder();

    /**
     * @return the Bolt URI to a Neo4j instance
     */
    @Nullable
    String getUri();

    /**
     * @return the Neo4j instance's username
     */
    @Nullable
    String getUser();

    /**
     * @return the Neo4j instance's password
     */
    @Nullable
    String getPassword();
}
