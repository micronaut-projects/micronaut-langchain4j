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
package io.micronaut.langchain4j.jdbc.memory;

import dev.langchain4j.community.store.memory.chat.sql.H2Dialect;
import dev.langchain4j.community.store.memory.chat.sql.MySQLDialect;
import dev.langchain4j.community.store.memory.chat.sql.PostgreSQLDialect;
import dev.langchain4j.community.store.memory.chat.sql.SQLChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.sql.SQLDialect;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.DisabledBeanException;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Creates the JDBC chat memory stores.
 */
@Factory
@Internal
final class JdbcChatMemoryStoreFactory {

    @Singleton
    @Bean(typed = {ChatMemoryStore.class, SQLChatMemoryStore.class})
    @EachBean(JdbcChatMemoryStoreConfiguration.class)
    SQLChatMemoryStore chatMemoryStore(JdbcChatMemoryStoreConfiguration configuration) {
        if (!configuration.isEnabled()) {
            throw new DisabledBeanException("The JDBC chat memory store is disabled");
        }
        JdbcChatMemoryStoreConfiguration.Dialect configured = configuration.getDialect();
        JdbcChatMemoryStoreConfiguration.Dialect dialect = configured != null ? configured : detect(configuration);
        return configuration.getBuilder()
            .sqlDialect(sqlDialect(dialect))
            .build();
    }

    private static SQLDialect sqlDialect(JdbcChatMemoryStoreConfiguration.Dialect dialect) {
        return switch (dialect) {
            case POSTGRESQL -> new PostgreSQLDialect();
            case MYSQL -> new MySQLDialect();
            case H2 -> new H2Dialect();
        };
    }

    private static JdbcChatMemoryStoreConfiguration.Dialect detect(JdbcChatMemoryStoreConfiguration configuration) {
        try (Connection connection = configuration.getDataSource().getConnection()) {
            String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
            if (product.contains("postgres")) {
                return JdbcChatMemoryStoreConfiguration.Dialect.POSTGRESQL;
            }
            if (product.contains("mysql") || product.contains("mariadb")) {
                return JdbcChatMemoryStoreConfiguration.Dialect.MYSQL;
            }
            if (product.contains("h2")) {
                return JdbcChatMemoryStoreConfiguration.Dialect.H2;
            }
            if (product.contains("oracle")) {
                throw new ConfigurationException("The JDBC chat memory store does not support Oracle Database: use the Oracle chat memory store of micronaut-langchain4j-store-oracle ("
                    + "langchain4j.chat-memory-store.oracle.<datasource>)");
            }
            throw new ConfigurationException("Unsupported database " + product + " for the JDBC chat memory store: configure "
                + JdbcChatMemoryStoreConfiguration.PREFIX + ".<datasource>.dialect");
        } catch (SQLException e) {
            throw new ConfigurationException("Cannot detect the SQL dialect of the JDBC chat memory store: " + e.getMessage(), e);
        }
    }
}
