/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.langchain4j.mongodb.memory;

import com.mongodb.client.MongoClient;
import dev.langchain4j.community.store.memory.chat.mongodb.MongoDbChatMemoryStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;

/**
 * Creates the MongoDB chat memory stores.
 */
@Factory
@Internal
final class MongoDbChatMemoryStoreFactory {

    private final BeanContext beanContext;

    MongoDbChatMemoryStoreFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Singleton
    @Bean(typed = {ChatMemoryStore.class, MongoDbChatMemoryStore.class})
    @EachBean(MongoDbChatMemoryStoreConfiguration.class)
    MongoDbChatMemoryStore chatMemoryStore(MongoDbChatMemoryStoreConfiguration configuration) {
        if (!configuration.isEnabled()) {
            throw new DisabledBeanException("The MongoDB chat memory store is disabled");
        }
        String server = configuration.getServer();
        MongoClient client = server == null
            ? beanContext.getBean(MongoClient.class)
            : beanContext.getBean(MongoClient.class, Qualifiers.byName(server));
        return configuration.getBuilder()
            .mongoClient(client)
            .build();
    }
}
