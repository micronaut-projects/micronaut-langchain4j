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
package io.micronaut.langchain4j.agentic;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;

/**
 * Configuration for a named agentic service agent.
 */
@EachProperty(AgentConfiguration.PREFIX)
public final class AgentConfiguration {

    public static final String PREFIX = "langchain4j.agentic.agents";

    private final String name;
    private final MemoryConfiguration memory = new MemoryConfiguration();

    @Nullable
    private String chatModel;

    /**
     * @param name The agent configuration name
     */
    public AgentConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The agent configuration name
     */
    public String getName() {
        return name;
    }

    /**
     * @return The chat model bean name to use for this agent
     */
    @Nullable
    public String getChatModel() {
        return chatModel;
    }

    /**
     * @param chatModel The chat model bean name to use for this agent
     */
    public void setChatModel(@Nullable String chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * @return The chat memory configuration
     */
    public MemoryConfiguration getMemory() {
        return memory;
    }

    /**
     * Configuration for agent chat memory.
     */
    @ConfigurationProperties("memory")
    public static final class MemoryConfiguration {

        @Nullable
        private String store;

        @Nullable
        private Integer maxMessages;

        /**
         * @return The chat memory store name
         */
        @Nullable
        public String getStore() {
            return store;
        }

        /**
         * @param store The chat memory store name
         */
        public void setStore(@Nullable String store) {
            this.store = store;
        }

        /**
         * @return The maximum number of messages to retain
         */
        @Nullable
        public Integer getMaxMessages() {
            return maxMessages;
        }

        /**
         * @param maxMessages The maximum number of messages to retain
         */
        public void setMaxMessages(@Nullable Integer maxMessages) {
            this.maxMessages = maxMessages;
        }
    }
}
