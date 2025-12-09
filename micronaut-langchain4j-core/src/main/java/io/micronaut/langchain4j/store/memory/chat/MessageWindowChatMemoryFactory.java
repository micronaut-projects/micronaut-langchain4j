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
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.NonNull;

/**
 * Utility class to obtain instances of {@link MessageWindowChatMemory.Builder}.
 * The {@code maxMessages} will be populated with the bean {@link MessageWindowChatMemoryConfiguration} whose value can be set via configuration.
 */
@Factory
@Internal
public class MessageWindowChatMemoryFactory {

    /**
     *
     * @param chatMemoryStore Chat Memory Store
     * @return An instance of {@link MessageWindowChatMemory.Builder} with maxMessages already set with the value of {@link MessageWindowChatMemoryConfiguration#getMaxMessages()} and the supplied Chat memory store.
     */
    @Prototype
    @EachBean(ChatMemoryStore.class)
    public MessageWindowChatMemory.@NonNull Builder createMessageWindowChatMemoryBuilder(@NonNull ChatMemoryStore chatMemoryStore,
                                                                         @NonNull MessageWindowChatMemoryConfiguration config) {
        return MessageWindowChatMemory.builder()
            .maxMessages(config.getMaxMessages())
            .chatMemoryStore(chatMemoryStore);
    }
}
