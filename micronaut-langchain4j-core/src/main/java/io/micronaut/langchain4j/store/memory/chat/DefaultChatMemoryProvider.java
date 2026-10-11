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
package io.micronaut.langchain4j.store.memory.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The default {@link ChatMemoryProvider} of the AI services, configured with {@link ChatMemoryConfiguration}.
 *
 * <p>The conversations are kept in the configured chat memory store, otherwise in the persistent chat memory store if
 * the application has exactly one, otherwise in memory.</p>
 */
@Singleton
@Primary
@Named(DefaultChatMemoryProvider.NAME)
@Internal
@Requires(property = ChatMemoryConfiguration.PREFIX + ".enabled", notEquals = StringUtils.FALSE)
@Requires(beans = ChatMemoryStore.class)
final class DefaultChatMemoryProvider implements ChatMemoryProvider {

    static final String NAME = "default";
    private static final String IN_MEMORY = "inMemory";

    private final BeanContext beanContext;
    private final ChatMemoryConfiguration configuration;
    private final MessageWindowChatMemoryConfiguration messageWindowConfiguration;
    private final AtomicReference<ChatMemoryStore> store = new AtomicReference<>();

    DefaultChatMemoryProvider(BeanContext beanContext,
                              ChatMemoryConfiguration configuration,
                              MessageWindowChatMemoryConfiguration messageWindowConfiguration) {
        this.beanContext = beanContext;
        this.configuration = configuration;
        this.messageWindowConfiguration = messageWindowConfiguration;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        ChatMemoryStore chatMemoryStore = store();
        return switch (configuration.getType()) {
            case MESSAGE_WINDOW -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(Objects.requireNonNullElse(configuration.getMaxMessages(), messageWindowConfiguration.getMaxMessages()))
                .chatMemoryStore(chatMemoryStore)
                .build();
            case TOKEN_WINDOW -> {
                Integer maxTokens = configuration.getMaxTokens();
                if (maxTokens == null) {
                    throw new ConfigurationException(ChatMemoryConfiguration.PREFIX + ".max-tokens is required with the TOKEN_WINDOW type");
                }
                yield TokenWindowChatMemory.builder()
                    .id(memoryId)
                    .maxTokens(maxTokens, beanContext.getBean(TokenCountEstimator.class))
                    .chatMemoryStore(chatMemoryStore)
                    .build();
            }
        };
    }

    private ChatMemoryStore store() {
        ChatMemoryStore chatMemoryStore = store.get();
        if (chatMemoryStore == null) {
            store.compareAndSet(null, resolveStore());
            chatMemoryStore = store.get();
        }
        return chatMemoryStore;
    }

    private ChatMemoryStore resolveStore() {
        String name = configuration.getStore();
        if (name != null) {
            return beanContext.getBean(ChatMemoryStore.class, Qualifiers.byName(name));
        }
        // the persistent stores are the ones other than the in-memory store
        List<BeanDefinition<ChatMemoryStore>> persistent = beanContext.getBeanDefinitions(ChatMemoryStore.class).stream()
            .filter(definition -> !IN_MEMORY.equals(definition.stringValue(Named.class).orElse(null)))
            .toList();
        if (persistent.size() == 1) {
            return beanContext.getBean(persistent.getFirst());
        }
        return beanContext.getBean(ChatMemoryStore.class, Qualifiers.byName(IN_MEMORY));
    }
}
