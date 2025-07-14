package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.store.memory.chat.redis.RedisChatMemoryStoreConfiguration;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class RedisChatMemoryStoreConfigurationTest {
    @Test
    void redisChatMemoryConfiguration(RedisChatMemoryStoreConfiguration config) {
        assertTrue(config.isEnabled());
        assertNull(config.getHost());
        assertNull(config.getUser());
        assertNull(config.getPassword());
        assertNull(config.getPrefix());
        assertNull(config.getTtl());
        assertNull(config.getPort());

    }
}
