package io.micronaut.langchain4j.store.memory.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.memory.chat.TokenWindowChatMemory;
import dev.langchain4j.model.TokenCountEstimator;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultChatMemoryProviderTest {

    @Test
    void usesTheInMemoryStoreByDefault() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("langchain4j.chat-memory.max-messages", 2))) {
            ChatMemoryProvider provider = context.getBean(ChatMemoryProvider.class);
            ChatMemory memory = provider.get("conversation");
            assertInstanceOf(MessageWindowChatMemory.class, memory);
            for (int i = 0; i < 3; i++) {
                memory.add(UserMessage.from("message " + i));
            }
            assertEquals(2, memory.messages().size());
            // the store is shared: another memory for the same conversation sees the messages
            assertEquals(2, provider.get("conversation").messages().size());
        }
    }

    @Test
    void usesThePersistentStoreWhenThereIsExactlyOne() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("spec.name", "persistent"))) {
            context.getBean(ChatMemoryProvider.class).get("conversation").add(UserMessage.from("Hello"));
            assertEquals(1, context.getBean(RecordingStore.class).updates.size());
        }
    }

    @Test
    void usesTheNamedStore() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spec.name", "persistent");
        properties.put("langchain4j.chat-memory.store", "inMemory");
        try (ApplicationContext context = ApplicationContext.run(properties)) {
            context.getBean(ChatMemoryProvider.class).get("conversation").add(UserMessage.from("Hello"));
            assertTrue(context.getBean(RecordingStore.class).updates.isEmpty());
            assertSame(context.getBean(ChatMemoryStore.class, io.micronaut.inject.qualifiers.Qualifiers.byName("inMemory")),
                context.getBean(ChatMemoryStore.class, io.micronaut.inject.qualifiers.Qualifiers.byName("inMemory")));
        }
    }

    @Test
    void tokenWindowMemoryRequiresMaxTokens() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", "tokens",
            "langchain4j.chat-memory.type", "token-window"))) {
            ChatMemoryProvider provider = context.getBean(ChatMemoryProvider.class);
            ConfigurationException error = assertThrows(ConfigurationException.class, () -> provider.get("conversation"));
            assertTrue(error.getMessage().contains("langchain4j.chat-memory.max-tokens is required"), error.getMessage());
        }
    }

    @Test
    void canBeDisabled() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("langchain4j.chat-memory.enabled", false))) {
            assertFalse(context.getBean(ChatMemoryConfiguration.class).isEnabled());
            assertFalse(context.containsBean(DefaultChatMemoryProvider.class));
        }
    }

    @Test
    void tokenWindowMemory() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", "tokens",
            "langchain4j.chat-memory.type", "token-window",
            "langchain4j.chat-memory.max-tokens", 2))) {
            ChatMemory memory = context.getBean(ChatMemoryProvider.class).get("conversation");
            assertInstanceOf(TokenWindowChatMemory.class, memory);
            for (int i = 0; i < 3; i++) {
                memory.add(UserMessage.from("message " + i));
            }
            // each message counts as one token
            assertEquals(2, memory.messages().size());
        }
    }

    @Singleton
    @Named("recording")
    @Requires(property = "spec.name", value = "persistent")
    static class RecordingStore extends InMemoryChatMemoryStore {
        final List<List<ChatMessage>> updates = new java.util.concurrent.CopyOnWriteArrayList<>();

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            updates.add(messages);
            super.updateMessages(memoryId, messages);
        }
    }

    @Factory
    @Requires(property = "spec.name", value = "tokens")
    static class EstimatorFactory {
        @Singleton
        TokenCountEstimator tokenCountEstimator() {
            return new TokenCountEstimator() {
                @Override
                public int estimateTokenCountInText(String text) {
                    return 1;
                }

                @Override
                public int estimateTokenCountInMessage(ChatMessage message) {
                    return 1;
                }

                @Override
                public int estimateTokenCountInMessages(Iterable<ChatMessage> messages) {
                    int count = 0;
                    for (var _ : messages) {
                        count++;
                    }
                    return count;
                }
            };
        }
    }
}
