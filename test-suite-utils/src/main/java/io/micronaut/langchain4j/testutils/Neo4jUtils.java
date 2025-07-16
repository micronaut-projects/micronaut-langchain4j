package io.micronaut.langchain4j.testutils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public final class Neo4jUtils {
    private static final Logger LOG = LoggerFactory.getLogger(Neo4jUtils.class);
    private static Neo4jContainer<?> container;

    private Neo4jUtils() {
    }

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    public static Map<String, Object> getProperties() throws InterruptedException {
        if (container == null) {
            container = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26"))
                .withEnv("NEO4J_PLUGINS", "[\"apoc\"]")
                .withEnv("NEO4J_apoc_export_file_enabled", "true")
                .withEnv("NEO4J_apoc_import_file_enabled", "true")
                .withEnv("NEO4J_dbms_security_procedures_unrestricted", "apoc.*")
                .withEnv("NEO4J_dbms_security_procedures_allowlist", "apoc.*");
            container.start();
            do {
                LOG.info("Waiting for Neo4j container to be ready...");
                Thread.sleep(1000);
            } while (!container.isRunning());
            GlobalTestLifecycle.registerShutdownHook();
        }
        return Map.of(
            "langchain4j.chat-memory-store.neo4j.uri", container.getBoltUrl(),
            "langchain4j.chat-memory-store.neo4j.user", "neo4j",
            "langchain4j.chat-memory-store.neo4j.password", container.getAdminPassword()
        );
    }

}
