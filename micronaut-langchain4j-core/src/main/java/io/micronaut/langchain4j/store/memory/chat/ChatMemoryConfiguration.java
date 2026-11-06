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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.util.Toggleable;
import org.jspecify.annotations.Nullable;

/**
 * Configures the default {@link dev.langchain4j.memory.chat.ChatMemoryProvider} of the AI services: the chat memory
 * store the conversations are kept in, and how the memory evicts old messages.
 *
 * @since 2.4.0
 */
@ConfigurationProperties(ChatMemoryConfiguration.PREFIX)
public final class ChatMemoryConfiguration implements Toggleable {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.chat-memory";

    /**
     * How the memory evicts old messages.
     */
    public enum Type {
        /**
         * Keeps the last {@code max-messages} messages.
         */
        MESSAGE_WINDOW,
        /**
         * Keeps the last messages that fit in {@code max-tokens} tokens, counted with the {@code TokenCountEstimator}
         * bean.
         */
        TOKEN_WINDOW
    }

    private boolean enabled = true;
    private @Nullable String store;
    private Type type = Type.MESSAGE_WINDOW;
    private @Nullable Integer maxMessages;
    private @Nullable Integer maxTokens;

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled Whether the default chat memory provider is enabled. Defaults to {@code true}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return The name of the chat memory store bean
     */
    public @Nullable String getStore() {
        return store;
    }

    /**
     * @param store The name of the {@code ChatMemoryStore} bean the conversations are kept in. Defaults to the
     *              persistent chat memory store if there is exactly one (Redis, Neo4j, Cassandra, JDBC...), otherwise the
     *              in-memory store.
     */
    public void setStore(@Nullable String store) {
        this.store = store;
    }

    /**
     * @return How the memory evicts old messages
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type How the memory evicts old messages. Defaults to {@code MESSAGE_WINDOW}.
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return The maximum number of messages of a message window memory
     */
    public @Nullable Integer getMaxMessages() {
        return maxMessages;
    }

    /**
     * @param maxMessages The maximum number of messages of a message window memory. Defaults to
     *                    {@code langchain4j.chat-memory-store.message-window.max-messages} (20).
     */
    public void setMaxMessages(@Nullable Integer maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * @return The maximum number of tokens of a token window memory
     */
    public @Nullable Integer getMaxTokens() {
        return maxTokens;
    }

    /**
     * @param maxTokens The maximum number of tokens of a token window memory. Required with {@code TOKEN_WINDOW}.
     */
    public void setMaxTokens(@Nullable Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
}
