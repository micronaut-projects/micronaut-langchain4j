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
package io.micronaut.langchain4j.store.memory.chat.cassandra;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;

import java.util.List;

/**
 * {@link ConfigurationProperties} implementation for {@link CassandraChatMemoryStoreConfiguration}.
 */
@Internal
@ConfigurationProperties(CassandraChatMemoryStoreConfigurationProperties.PREFIX)
public class CassandraChatMemoryStoreConfigurationProperties implements CassandraChatMemoryStoreConfiguration {
    /**
     * Default port.
     */
    public static final Integer DEFAULT_PORT = 9042;
    /**
     * Default table name.
     */
    public static final String DEFAULT_TABLE_NAME = "message_store";
    public static final String PREFIX = "langchain4j.store-memory-chat.cassandra";
    public static final boolean DEFAULT_ENABLED = true;
    public static final String PROPERTY_ENABLED = PREFIX + ".enabled";
    private boolean enabled = DEFAULT_ENABLED;
    private List<String> contactPoints;
    private String localDataCenter;
    private Integer port = DEFAULT_PORT;
    private String userName;
    private String password;
    private String keyspace;
    private String table = DEFAULT_TABLE_NAME;

    /**
     *
     * @return enabled Whether Neo4j ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @param enabled Whether Neo4j ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public List<String> getContactPoints() {
        return contactPoints;
    }

    /**
     *
     * @param contactPoints Contact Points
     */
    public void setContactPoints(List<String> contactPoints) {
        this.contactPoints = contactPoints;
    }

    @Override
    public String getLocalDataCenter() {
        return localDataCenter;
    }

    /**
     *
     * @param localDataCenter Local Data Center
     */
    public void setLocalDataCenter(String localDataCenter) {
        this.localDataCenter = localDataCenter;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    /**
     *
     * @param port Port
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    /**
     *
     * @param userName Username
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password Password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getKeyspace() {
        return keyspace;
    }

    /**
     *
     * @param keyspace Keyspace
     */
    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    @Override
    public String getTable() {
        return table;
    }

    /**
     *
     * @param table table
     */
    public void setTable(String table) {
        this.table = table;
    }
}
