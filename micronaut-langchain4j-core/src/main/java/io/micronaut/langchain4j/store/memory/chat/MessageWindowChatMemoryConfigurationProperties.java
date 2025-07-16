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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

/**
 * {@link ConfigurationProperties} for {@link MessageWindowChatMemoryConfiguration}.
 */
@Internal
@ConfigurationProperties(MessageWindowChatMemoryConfigurationProperties.PREFIX)
public class MessageWindowChatMemoryConfigurationProperties implements MessageWindowChatMemoryConfiguration {
    public static final String PREFIX = "langchain4j.store-memory-chat.message-window";
    private Integer maxMessages = DEFAULT_MAX_MESSAGES;

    @Override
    @NonNull
    public Integer getMaxMessages() {
        return maxMessages;
    }

    /**
     * Sets the maximum number of messages to retain. Default value 20.
     * If there isn't enough space for a new message, the oldest one is evicted.
     * @param maxMessages The maximum number of messages to retain.
     */
    public void setMaxMessages(@NonNull Integer maxMessages) {
        this.maxMessages = maxMessages;
    }
}
