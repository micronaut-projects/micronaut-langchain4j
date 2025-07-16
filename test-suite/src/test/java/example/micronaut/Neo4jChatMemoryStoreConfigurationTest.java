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
        assertNotNull(config.getBuilder());
        assertNull(config.getUri());
        assertNull(config.getUser());
        assertNull(config.getPassword());
    }
}
