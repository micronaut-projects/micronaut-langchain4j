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
package io.micronaut.langchain4j.store.memory.chat.neo4j;

import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;

/**
 * Configuration for {@link dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore.Builder}.
 */
public interface Neo4jChatMemoryStoreConfiguration extends Toggleable {
    /**
     * @return the node label to be used for the memory ID
     */
    @Nullable
    String getMemoryLabel();

    /**
     * @return the node label to be used for the message
     */
    @Nullable
    String getMessageLabel();

    /**
     * @return the optional memory ID property name of the node
     */
    @Nullable
    String getIdProperty();

    /**
     * @return the property name to be used for the message text
     */
    @Nullable
    String getMessageProperty();

    /**
     * @return the relationship type to be used to store the last message
     */
    @Nullable
    String getLastMessageRelType();

    /**
     * @return the relationship type to be used to store the next messages
     */
    @Nullable
    String getNextMessageRelType();

    /**
     * @return the optional database name
     */
    @Nullable
    String getDatabaseName();

    /**
     * @return the optional message size to be retrieved from {@link Neo4jChatMemoryStore#getMessages(Object)}}.
     */
    @Nullable
    Integer getSize();

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
