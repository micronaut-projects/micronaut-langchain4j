package io.micronaut.langchain4j.store.memory.chat.inmemory;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(startApplication = false)
class InMemoryMessageWindowChatMemoryProviderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void messageWindowChatMemoryProvider() {
        ChatMemoryProvider provider = beanContext.getBean(ChatMemoryProvider.class, Qualifiers.byName("InMemoryMessageWindow"));
        assertNotNull(provider);
        assertInstanceOf(InMemoryMessageWindowChatMemoryProvider.class, provider);
    }
}
