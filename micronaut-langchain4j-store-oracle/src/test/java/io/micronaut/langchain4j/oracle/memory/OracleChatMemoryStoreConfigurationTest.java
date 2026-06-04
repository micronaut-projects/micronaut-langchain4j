package io.micronaut.langchain4j.oracle.memory;

import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class OracleChatMemoryStoreConfigurationTest {

    @Test
    void oracleChatMemoryConfiguration() {
        try (ApplicationContext applicationContext = ApplicationContext.run()) {
            OracleChatMemoryStoreConfiguration config = applicationContext.getBean(OracleChatMemoryStoreConfiguration.class);
            assertTrue(config.isEnabled());
            assertNotNull(config.getBuilder());
        }
    }
}
