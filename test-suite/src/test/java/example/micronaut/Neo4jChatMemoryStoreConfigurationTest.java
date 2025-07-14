package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.store.memory.chat.neo4j.Neo4jChatMemoryStoreConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class Neo4jChatMemoryStoreConfigurationTest {
    @Test
    void neo4jChatMemoryConfiguration(Neo4jChatMemoryStoreConfiguration config) {
        assertTrue(config.isEnabled());
        assertNull(config.getSize());
        assertNull(config.getMemoryLabel());
        assertNull(config.getMessageLabel());
        assertNull(config.getIdProperty());
        assertNull(config.getMessageProperty());
        assertNull(config.getLastMessageRelType());
        assertNull(config.getNextMessageRelType());
        assertNull(config.getDatabaseName());
        assertNull(config.getSize());
        assertNull(config.getUri());
        assertNull(config.getUser());
        assertNull(config.getPassword());
    }
}
