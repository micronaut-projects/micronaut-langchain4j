package io.micronaut.langchain4j.redis.memory;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class RedisChatMemoryStoreConfigurationTest {
    @Test
    void redisChatMemoryConfiguration(RedisChatMemoryStoreConfiguration config) {
        assertTrue(config.isEnabled());
        assertNotNull(config.getBuilder());
    }
}
