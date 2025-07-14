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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;

/**
 * {@link ConfigurationProperties} implementation for {@link RedisChatMemoryStoreConfiguration}.
 */
@Internal
@ConfigurationProperties(RedisChatMemoryStoreConfigurationProperties.PREFIX)
public class RedisChatMemoryStoreConfigurationProperties implements RedisChatMemoryStoreConfiguration {
    public static final String PREFIX = "langchain4j.store-memory-chat.redis";
    public static final boolean DEFAULT_ENABLED = true;
    public static final String PROPERTY_ENABLED = PREFIX + ".enabled";
    private String host;
    private String password;
    private String user;
    private Long ttl;
    private Integer port;
    private String prefix;
    private boolean enabled = DEFAULT_ENABLED;

    /**
     *
     * @return Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled Whether Redis ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getHost() {
        return host;
    }

    /**
     * Sets the Redis host.
     *
     * @param host The Redis server hostname or IP address
     */
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Sets the Redis password for authentication.
     * @param password The Redis password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUser() {
        return user;
    }

    /**
     * Sets the Redis user for authentication.
     *
     * @param user The Redis username
     */
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public Long getTtl() {
        return ttl;
    }

    /**
     * Sets the Time-To-Live (TTL) value for the Redis keys.
     * This value determines how long the keys will persist in Redis before being automatically deleted.
     *
     * @param ttl The TTL value in seconds. A value of 0 or fewer means the keys will not expire.
     */
    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the Redis port.
     *
     * @param port The Redis server port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix to be used for Redis keys.
     * This prefix is prepended to all keys stored in Redis, allowing for better organization or namespacing.
     * Usually would end with a colon. ex "chat:"
     *
     * @param prefix The prefix string to be added to Redis keys.
     *
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
