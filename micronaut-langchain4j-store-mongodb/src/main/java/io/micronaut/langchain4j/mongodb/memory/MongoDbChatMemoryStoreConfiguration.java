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

import dev.langchain4j.community.store.memory.chat.mongodb.MongoDbChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.util.Toggleable;
import org.jspecify.annotations.Nullable;

/**
 * Configures a MongoDB chat memory store, with the {@code database-name}, {@code collection-name} and
 * {@code expire-after-seconds} properties.
 *
 * @since 2.4.0
 */
@EachProperty(value = MongoDbChatMemoryStoreConfiguration.PREFIX, primary = "default")
public final class MongoDbChatMemoryStoreConfiguration implements Toggleable {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.chat-memory-store.mongodb";

    @ConfigurationBuilder(prefixes = "", excludes = "mongoClient")
    MongoDbChatMemoryStore.Builder builder = MongoDbChatMemoryStore.builder();

    private final String name;
    private boolean enabled = true;
    private @Nullable String server;

    /**
     * @param name The name of the store
     */
    public MongoDbChatMemoryStoreConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the store
     */
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled Whether the MongoDB chat memory store is enabled. Defaults to {@code true}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return The name of the MongoDB server, or {@code null} for the default server
     */
    public @Nullable String getServer() {
        return server;
    }

    /**
     * @param server The name of the MongoDB server ({@code mongodb.servers.<name>}). Defaults to the default server.
     */
    public void setServer(@Nullable String server) {
        this.server = server;
    }

    /**
     * @return The builder of the store
     */
    public MongoDbChatMemoryStore.Builder getBuilder() {
        return builder;
    }
}
