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

import dev.langchain4j.community.store.memory.chat.sql.SQLChatMemoryStore;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.util.Toggleable;
import org.jspecify.annotations.Nullable;

import javax.sql.DataSource;

/**
 * Configures a JDBC {@link SQLChatMemoryStore} for a data source. The name of the configuration is the name of the
 * data source.
 *
 * @since 2.4.0
 */
@EachProperty(value = JdbcChatMemoryStoreConfiguration.PREFIX, primary = "default")
public final class JdbcChatMemoryStoreConfiguration implements Toggleable {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.chat-memory-store.jdbc";

    /**
     * The SQL dialects.
     */
    public enum Dialect {
        /**
         * PostgreSQL.
         */
        POSTGRESQL,
        /**
         * MySQL and MariaDB.
         */
        MYSQL,
        /**
         * H2.
         */
        H2
    }

    @ConfigurationBuilder(prefixes = "", excludes = {"dataSource", "sqlDialect"})
    SQLChatMemoryStore.SQLChatMemoryStoreBuilder builder;

    private final DataSource dataSource;
    private boolean enabled = true;
    private @Nullable Dialect dialect;

    /**
     * @param dataSource The data source with the same name as the configuration
     */
    public JdbcChatMemoryStoreConfiguration(@Parameter DataSource dataSource) {
        this.dataSource = dataSource;
        this.builder = SQLChatMemoryStore.builder().dataSource(dataSource);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled Whether the JDBC chat memory store is enabled. Defaults to {@code true}.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return The SQL dialect, or {@code null} to detect it from the database
     */
    public @Nullable Dialect getDialect() {
        return dialect;
    }

    /**
     * @param dialect The SQL dialect. Detected from the database by default.
     */
    public void setDialect(@Nullable Dialect dialect) {
        this.dialect = dialect;
    }

    /**
     * @return The data source
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return The builder of the store, configured with the {@code table-name}, {@code auto-create-table},
     * {@code memory-id-column-name} and {@code content-column-name} properties
     */
    public SQLChatMemoryStore.SQLChatMemoryStoreBuilder getBuilder() {
        return builder;
    }
}
