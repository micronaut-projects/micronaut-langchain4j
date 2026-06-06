package io.micronaut.langchain4j.oracle.memory;

import dev.langchain4j.store.memory.chat.oracle.OracleChatMemoryStore;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.oracle.default.enabled", value = StringUtils.TRUE)
@MicronautTest(startApplication = false)
class OracleChatMemoryStoreConfigurationTest {

    @Test
    void oracleChatMemoryConfiguration(@Named("default") OracleChatMemoryStoreConfiguration config,
                                       @Named("default") OracleChatMemoryStore chatMemoryStore) {
        assertTrue(config.isEnabled());
        assertNotNull(config.getBuilder());
        assertNotNull(chatMemoryStore);
    }

    @Property(name = "langchain4j.chat-memory-store.oracle.reporting.enabled", value = StringUtils.FALSE)
    @Test
    void disabledOracleChatMemoryConfigurationDoesNotCreateStore(BeanContext beanContext) {
        assertTrue(beanContext.containsBean(OracleChatMemoryStore.class, Qualifiers.byName("default")));
        assertTrue(beanContext.containsBean(OracleChatMemoryStoreConfiguration.class, Qualifiers.byName("reporting")));
        assertTrue(beanContext.containsBean(OracleChatMemoryStoreConfiguration.class, Qualifiers.byName("default")));
        assertFalse(beanContext.getBean(OracleChatMemoryStoreConfiguration.class, Qualifiers.byName("reporting")).isEnabled());
        assertFalse(beanContext.containsBean(OracleChatMemoryStore.class, Qualifiers.byName("reporting")));
    }
}
