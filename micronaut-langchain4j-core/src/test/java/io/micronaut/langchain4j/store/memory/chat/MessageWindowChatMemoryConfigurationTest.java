package io.micronaut.langchain4j.store.memory.chat;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class MessageWindowChatMemoryConfigurationTest {
    @Test
    void maxMessagesDefaultsTo20(MessageWindowChatMemoryConfiguration config) {
        assertEquals(20, config.getMaxMessages());

        if (config instanceof MessageWindowChatMemoryConfigurationProperties configurationProperties) {
            assertThrows(NullPointerException.class, () -> configurationProperties.setMaxMessages(null));
        }
    }
}
