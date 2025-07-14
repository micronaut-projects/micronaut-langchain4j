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

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;

/**
 * Configuration for {@link dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore.Builder}.
 */
public interface RedisChatMemoryStoreConfiguration extends Toggleable {
    /**
     * Returns the Redis host.
     *
     * @return The Redis server hostname or IP address
     */
    @Nullable
    String getHost();

    /**
     * @return The Redis password
     */
    @Nullable
    String getPassword();

    /**
     * @return The Redis username
     */
    @Nullable
    String getUser();

    /**
     * Returns the Time-To-Live (TTL) value for the Redis keys.
     * This value determines how long the keys will persist in Redis before being automatically deleted.
     *
     * @return The TTL value in seconds. A value of 0 or fewer means the keys will not expire.
     */
    @Nullable
    Long getTtl();

    /**
     * @return The Redis server port
     */
    @Nullable
    Integer getPort();

    /**
     * Returns the prefix to be used for Redis keys.
     * This prefix is prepended to all keys stored in Redis, allowing for better organization or namespacing.
     * Usually would end with a colon. ex "chat:"
     *
     * @return The prefix string to be added to Redis keys.
     *
     */
    @Nullable
    String getPrefix();
}
