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
package io.micronaut.langchain4j.store.memory.chat;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

/**
 * Utility class to obtain instances of {@link MessageWindowChatMemory.Builder}.
 * The {@code maxMessages} will be populated with the bean {@link MessageWindowChatMemoryConfiguration} whose value can be set via configuration.
 */
@Singleton
public class MessageWindowChatMemoryFactory {
    private final MessageWindowChatMemoryConfiguration config;

    /**
     *
     * @param config Message Window Chat Memory Configuration
     */
    public MessageWindowChatMemoryFactory(MessageWindowChatMemoryConfiguration config) {
        this.config = config;
    }

    /**
     *
     * @param chatMemoryStore Chat Memory Store
     * @return An instance of {@link MessageWindowChatMemory.Builder} with maxMessages already set with the value of {@link MessageWindowChatMemoryConfiguration#getMaxMessages()} and the supplied Chat memory store.
     */
    @NonNull
    public MessageWindowChatMemory.Builder withChatMemoryStore(@NonNull ChatMemoryStore chatMemoryStore) {
        return MessageWindowChatMemory.builder()
            .maxMessages(config.getMaxMessages())
            .chatMemoryStore(chatMemoryStore);
    }
}
