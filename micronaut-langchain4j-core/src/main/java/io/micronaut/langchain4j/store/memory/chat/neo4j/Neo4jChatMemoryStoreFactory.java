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
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Factory
@Internal
class Neo4jChatMemoryStoreFactory {
    @Named("neo4j")
    @Prototype
    Neo4jChatMemoryStore.Builder createRedisChatMemoryStoreBuilder(Neo4jChatMemoryStoreConfiguration config) {
        Neo4jChatMemoryStore.Builder builder = Neo4jChatMemoryStore.builder();
        if (StringUtils.isNotEmpty(config.getMemoryLabel())) {
            builder.memoryLabel(config.getMemoryLabel());
        }
        if (StringUtils.isNotEmpty(config.getMessageLabel())) {
            builder.messageLabel(config.getMessageLabel());
        }
        if (StringUtils.isNotEmpty(config.getIdProperty())) {
            builder.idProperty(config.getIdProperty());
        }
        if (StringUtils.isNotEmpty(config.getMessageProperty())) {
            builder.messageProperty(config.getMessageProperty());
        }
        if (StringUtils.isNotEmpty(config.getLastMessageRelType())) {
            builder.lastMessageRelType(config.getLastMessageRelType());
        }
        if (StringUtils.isNotEmpty(config.getNextMessageRelType())) {
            builder.nextMessageRelType(config.getNextMessageRelType());
        }
        if (StringUtils.isNotEmpty(config.getDatabaseName())) {
            builder.databaseName(config.getDatabaseName());
        }
        if (config.getSize() != null) {
            builder.size(config.getSize());
        }
        if (StringUtils.isNotEmpty(config.getUri())) {
            builder.withBasicAuth(config.getUri(), config.getUser(), config.getPassword());
        }
        return builder;
    }

    @Singleton
    @EachBean(Neo4jChatMemoryStore.Builder.class)
    Neo4jChatMemoryStore createRedisChatMemoryStore(Neo4jChatMemoryStore.Builder builder) {
        return builder.build();
    }
}
