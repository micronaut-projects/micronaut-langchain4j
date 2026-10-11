package io.micronaut.langchain4j.jdbc.memory;

import dev.langchain4j.community.store.memory.chat.sql.SQLChatMemoryStore;
import dev.langchain4j.data.message.UserMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.Qualifier;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The SQL dialect of the JDBC chat memory store: configured, or detected from the database.
 */
class JdbcChatMemoryStoreDialectTest {

    static final String SPEC_NAME = "JdbcChatMemoryStoreDialectTest";

    @Test
    void usesTheConfiguredDialect() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "datasources.default.url", "jdbc:h2:mem:dialect;DB_CLOSE_DELAY=-1",
            "datasources.default.username", "sa",
            "datasources.default.password", "",
            "datasources.default.driver-class-name", "org.h2.Driver",
            "langchain4j.chat-memory-store.jdbc.default.dialect", "H2",
            "langchain4j.chat-memory-store.jdbc.default.table-name", "chat_memory",
            "langchain4j.chat-memory-store.jdbc.default.auto-create-table", true))) {
            assertEquals(JdbcChatMemoryStoreConfiguration.Dialect.H2, context.getBean(JdbcChatMemoryStoreConfiguration.class).getDialect());
            SQLChatMemoryStore store = context.getBean(SQLChatMemoryStore.class);
            store.updateMessages("conversation", java.util.List.of(UserMessage.from("Hello")));
            assertEquals(1, store.getMessages("conversation").size());
        }
    }

    @Test
    void canBeDisabled() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "datasources.default.url", "jdbc:h2:mem:disabled;DB_CLOSE_DELAY=-1",
            "datasources.default.username", "sa",
            "datasources.default.password", "",
            "datasources.default.driver-class-name", "org.h2.Driver",
            "langchain4j.chat-memory-store.jdbc.default.enabled", false))) {
            assertFalse(context.getBean(JdbcChatMemoryStoreConfiguration.class).isEnabled());
            assertTrue(context.findBean(SQLChatMemoryStore.class).isEmpty());
        }
    }

    @Test
    void detectsTheDialectOfTheDatabase() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.chat-memory-store.jdbc.mysql.table-name", "chat_memory",
            "langchain4j.chat-memory-store.jdbc.oracle.table-name", "chat_memory",
            "langchain4j.chat-memory-store.jdbc.other.table-name", "chat_memory",
            "langchain4j.chat-memory-store.jdbc.broken.table-name", "chat_memory"))) {
            assertNotNull(context.getBean(SQLChatMemoryStore.class, Qualifiers.byName("mysql")));
            assertFailure(context, "oracle", "use the Oracle chat memory store of micronaut-langchain4j-store-oracle");
            assertFailure(context, "other", "Unsupported database other database for the JDBC chat memory store");
            assertFailure(context, "broken", "Cannot detect the SQL dialect of the JDBC chat memory store: no connection");
        }
    }

    private static void assertFailure(ApplicationContext context, String name, String message) {
        Qualifier<SQLChatMemoryStore> qualifier = Qualifiers.byName(name);
        BeanInstantiationException error = assertThrows(BeanInstantiationException.class, () -> context.getBean(SQLChatMemoryStore.class, qualifier));
        assertTrue(error.getMessage().contains(message), error.getMessage());
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class DataSources {

        @Singleton
        @Named("mysql")
        DataSource mysql() {
            return dataSource("MySQL");
        }

        @Singleton
        @Named("oracle")
        DataSource oracle() {
            return dataSource("Oracle");
        }

        @Singleton
        @Named("other")
        DataSource other() {
            return dataSource("Other Database");
        }

        @Singleton
        @Named("broken")
        DataSource broken() {
            return proxy(DataSource.class, (proxy, method, args) -> {
                if (method.getName().equals("getConnection")) {
                    throw new SQLException("no connection");
                }
                return null;
            });
        }

        private static DataSource dataSource(String product) {
            DatabaseMetaData metaData = proxy(DatabaseMetaData.class, (proxy, method, args) ->
                method.getName().equals("getDatabaseProductName") ? product : null);
            // the statements of the store succeed without doing anything
            java.sql.Statement statement = proxy(java.sql.PreparedStatement.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
            Connection connection = proxy(Connection.class, (proxy, method, args) -> switch (method.getName()) {
                case "getMetaData" -> metaData;
                case "createStatement", "prepareStatement" -> statement;
                default -> defaultValue(method.getReturnType());
            });
            return proxy(DataSource.class, (proxy, method, args) ->
                method.getName().equals("getConnection") ? connection : null);
        }

        private static Object defaultValue(Class<?> type) {
            if (type == boolean.class) {
                return false;
            }
            if (type == int.class) {
                return 0;
            }
            if (type == long.class) {
                return 0L;
            }
            return null;
        }

        private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
        }
    }
}
