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
package io.micronaut.langchain4j.store.memory.chat.neo4j;

import dev.langchain4j.community.store.memory.chat.neo4j.Neo4jChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Internal;

/**
 * {@link ConfigurationProperties} implementation for {@link Neo4jChatMemoryStoreConfiguration}.
 */
@Internal
@ConfigurationProperties(Neo4jChatMemoryStoreConfigurationProperties.PREFIX)
class Neo4jChatMemoryStoreConfigurationProperties implements Neo4jChatMemoryStoreConfiguration {
    public static final String PREFIX = "langchain4j.store-memory-chat.neo4j";
    public static final boolean DEFAULT_ENABLED = true;
    public static final String PROPERTY_ENABLED = PREFIX + ".enabled";
    private boolean enabled = DEFAULT_ENABLED;
    private String memoryLabel;
    private String messageLabel;
    private String lastMessageRelType;
    private String nextMessageRelType;
    private String idProperty;
    private String messageProperty;
    private String databaseName;
    private Integer size;
    private String uri;
    private String user;
    private String password;

    /**
     * Whether Neo4j ChatMemory store is enabled. Default value {@value #DEFAULT_ENABLED}.
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
    public String getMemoryLabel() {
        return memoryLabel;
    }

    /**
     * @param memoryLabel the node label to be used for the memory ID
     */
    public void setMemoryLabel(String memoryLabel) {
        this.memoryLabel = memoryLabel;
    }

    @Override
    public String getMessageLabel() {
        return messageLabel;
    }

    /**
     * @param  messageLabel the node label to be used for the message
     */
    public void setMessageLabel(String messageLabel) {
        this.messageLabel = messageLabel;
    }

    @Override
    public String getLastMessageRelType() {
        return lastMessageRelType;
    }

    /**
     * @param lastMessageRelType the relationship type to be used to store the last message
     */
    public void setLastMessageRelType(String lastMessageRelType) {
        this.lastMessageRelType = lastMessageRelType;
    }

    @Override
    public String getNextMessageRelType() {
        return nextMessageRelType;
    }

    /**
     * @param nextMessageRelType the relationship type to be used to store the next messages
     */
    public void setNextMessageRelType(String nextMessageRelType) {
        this.nextMessageRelType = nextMessageRelType;
    }

    @Override
    public String getIdProperty() {
        return idProperty;
    }

    /**
     * @param idProperty the optional memory ID property name of the node
     */
    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }

    @Override
    public String getMessageProperty() {
        return messageProperty;
    }

    /**
     * @param messageProperty the property name to be used for the message text
     */
    public void setMessageProperty(String messageProperty) {
        this.messageProperty = messageProperty;
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param databaseName the optional database name
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    /**
     * @param size the optional message size to be retrieved from {@link Neo4jChatMemoryStore#getMessages(Object)}}.
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    @Override
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the Bolt URI to a Neo4j instance
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getUser() {
        return user;
    }

    /**
     * @param user the Neo4j instance's username
     */
    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param password the Neo4j instance's password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
