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
package io.micronaut.langchain4j.store.memory.chat.inmemory;

import dev.langchain4j.Internal;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.langchain4j.store.memory.chat.MessageWindowChatMemoryConfiguration;
import io.micronaut.langchain4j.store.memory.chat.MessageWindowChatMemoryFactory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * {@link ChatMemoryProvider} implementation that creates {@link dev.langchain4j.memory.chat.MessageWindowChatMemory}.
 */
@Internal
@Named("InMemoryMessageWindow")
@Singleton
class InMemoryMessageWindowChatMemoryProvider implements ChatMemoryProvider {
    private final MessageWindowChatMemoryFactory messageWindowChatMemoryFactory;
    private final InMemoryChatMemoryStoreFactory inMemoryChatMemoryStoreFactory;
    private final MessageWindowChatMemoryConfiguration config;

    InMemoryMessageWindowChatMemoryProvider(MessageWindowChatMemoryFactory messageWindowChatMemoryFactory,
                                            InMemoryChatMemoryStoreFactory inMemoryChatMemoryStoreFactory,
                                            MessageWindowChatMemoryConfiguration config) {
        this.messageWindowChatMemoryFactory = messageWindowChatMemoryFactory;
        this.inMemoryChatMemoryStoreFactory = inMemoryChatMemoryStoreFactory;
        this.config = config;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        ChatMemoryStore chatMemoryStore = inMemoryChatMemoryStoreFactory.chatMemoryStore();
        return messageWindowChatMemoryFactory.createMessageWindowChatMemoryBuilder(chatMemoryStore, config)
            .id(memoryId)
            .build();
    }
}
