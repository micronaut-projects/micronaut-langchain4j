package io.micronaut.langchain4j.neo4j.memory;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.testutils.Neo4jUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Neo4jChatMemoryStoreConfigurationTest implements TestPropertyProvider {
    @Override
    public @NonNull Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> props = Neo4jUtils.getProperties();
            for (String k : props.keySet()) {
                result.put(k, props.get(k).toString());
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not set Neo4j properties", e);
        }
        return result;
    }

    @Test
    void neo4jChatMemoryConfiguration(Neo4jChatMemoryStoreConfiguration config) {
        assertTrue(config.isEnabled());
        assertNotNull(config.getBuilder());
        assertNotNull(config.getUri());
        assertNotNull(config.getUser());
        assertNotNull(config.getPassword());
    }
}
