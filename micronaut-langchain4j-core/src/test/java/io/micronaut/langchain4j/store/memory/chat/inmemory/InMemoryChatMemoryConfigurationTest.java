package io.micronaut.langchain4j.store.memory.chat.inmemory;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class InMemoryChatMemoryConfigurationTest {

    @Test
    void inMemoryChatMemoryConfiguration(InMemoryChatMemoryConfiguration config) {
        assertTrue(config.isEnabled());
    }
}
