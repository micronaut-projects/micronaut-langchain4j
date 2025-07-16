package io.micronaut.langchain4j.testutils;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.InetSocketAddress;
import java.util.Map;
import org.testcontainers.cassandra.CassandraContainer;
import org.testcontainers.utility.DockerImageName;

public final class CassandraUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CassandraUtils.class);
    static final String CASSANDRA_IMAGE = "cassandra:5.0";
    private static final String DATACENTER = "datacenter1";
    private static final String KEYSPACE = "langchain4j";
    private static CassandraContainer container;
    private static InetSocketAddress contactPoint;
    private CassandraUtils() {
    }

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public static Map<String, Object> getProperties() throws InterruptedException {
        if (container == null) {
            container = new CassandraContainer(DockerImageName.parse(CASSANDRA_IMAGE));
            container.start();
            do {
                LOG.info("Waiting for Cassandra container to be ready...");
                Thread.sleep(1000);
            } while (!container.isRunning());

            contactPoint = container.getContactPoint();
            try (CqlSession session = CqlSession.builder()
                .addContactPoint(contactPoint)
                .withLocalDatacenter(DATACENTER)
                .build()) {
                session.execute("CREATE KEYSPACE IF NOT EXISTS " + KEYSPACE + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};");
            }
            GlobalTestLifecycle.registerShutdownHook();
        }
        return Map.of(
            "langchain4j.chat-memory-store.cassandra.contact-points[0]", contactPoint.getHostName(),
            "langchain4j.chat-memory-store.cassandra.port", contactPoint.getPort(),
            "langchain4j.chat-memory-store.cassandra.local-data-center", DATACENTER,
            "langchain4j.chat-memory-store.cassandra.keyspace", KEYSPACE
        );
    }
}
