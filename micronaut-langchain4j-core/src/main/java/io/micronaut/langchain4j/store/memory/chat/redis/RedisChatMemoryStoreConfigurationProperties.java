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
package io.micronaut.langchain4j.store.memory.chat.redis;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;

/**
 * {@link ConfigurationProperties} implementation for {@link RedisChatMemoryStoreConfiguration}.
 */
@Internal
@ConfigurationProperties(RedisChatMemoryStoreConfigurationProperties.PREFIX)
class RedisChatMemoryStoreConfigurationProperties implements RedisChatMemoryStoreConfiguration {
    public static final String PREFIX = "langchain4j.store-memory-chat.redis";
    public static final boolean DEFAULT_ENABLED = true;
    public static final String PROPERTY_ENABLED = PREFIX + ".enabled";
    private boolean enabled = DEFAULT_ENABLED;
    @ConfigurationBuilder(prefixes = "")
    private RedisChatMemoryStore.Builder builder = RedisChatMemoryStore.builder();

    /**
     * Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     * @return Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     * @param enabled Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public RedisChatMemoryStore.Builder getBuilder() {
        return builder;
    }

    /**
     * An instance of RedisChatMemoryStore.Builder.
     * @param builder An instance of RedisChatMemoryStore.Builder
     */
    public void setBuilder(RedisChatMemoryStore.Builder builder) {
        this.builder = builder;
    }
}
