package io.micronaut.langchain4j.pgvector;

import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import io.micronaut.jdbc.DataSourceResolver;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PgVectorEmbeddingStoreFactoryTest {

    @Test
    void unwrapsDataSourceBeforeConstructingStore() {
        DataSource targetDataSource = new StubDataSource(false);
        DataSource contextualDataSource = new StubDataSource(true);
        DataSourceResolver dataSourceResolver = new DataSourceResolver() {
            @Override
            public DataSource resolve(DataSource dataSource) {
                return targetDataSource;
            }
        };
        PgVectorEmbeddingStoreFactory factory = new PgVectorEmbeddingStoreFactory(dataSourceResolver);
        PgVectorEmbeddingStoreConfig configuration = new PgVectorEmbeddingStoreConfig(contextualDataSource, null);
        configuration.setTable("test");
        configuration.setDimension(384);
        configuration.setCreateTable(false);
        configuration.setDropTableFirst(false);

        PgVectorEmbeddingStore embeddingStore = factory.pgVectorEmbeddingStore(configuration);

        Assertions.assertNotNull(embeddingStore);
    }

    private static final class StubDataSource implements DataSource {
        private final boolean failOnConnection;

        private StubDataSource(boolean failOnConnection) {
            this.failOnConnection = failOnConnection;
        }

        @Override
        public Connection getConnection() throws SQLException {
            if (failOnConnection) {
                throw new SQLException("No current connection present");
            }
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[] {Connection.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "createStatement" -> Proxy.newProxyInstance(
                                Statement.class.getClassLoader(),
                                new Class<?>[] {Statement.class},
                                (statementProxy, statementMethod, statementArgs) -> switch (statementMethod.getName()) {
                                    case "execute", "executeUpdate", "close" -> null;
                                    case "getConnection" -> proxy;
                                    case "unwrap" -> statementArgs[0] == Statement.class ? statementProxy : null;
                                    case "isWrapperFor" -> statementArgs[0] == Statement.class;
                                    default -> defaultValue(statementMethod.getReturnType());
                                }
                        );
                        case "close" -> null;
                        case "unwrap" -> args[0] == Connection.class ? proxy : null;
                        case "isWrapperFor" -> args[0] == Connection.class;
                        default -> defaultValue(method.getReturnType());
                    }
            );
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return getConnection();
        }

        @Override
        public PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(PrintWriter out) {
            // No-op: this test stub does not track mutable log-writer state.
        }

        @Override
        public void setLoginTimeout(int seconds) {
            // No-op: this test stub does not enforce login timeouts.
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            if (iface.isInstance(this)) {
                return iface.cast(this);
            }
            throw new SQLException("Unsupported unwrap target: " + iface.getName());
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return iface.isInstance(this);
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0F;
        }
        if (returnType == double.class) {
            return 0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
