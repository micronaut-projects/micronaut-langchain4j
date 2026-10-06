package io.micronaut.langchain4j.jdbc.memory;

import dev.langchain4j.community.store.memory.chat.sql.SQLChatMemoryStore;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * The JDBC chat memory store with H2, and the default chat memory provider that uses it.
 */
class JdbcChatMemoryStoreTest {

    @Test
    void persistsTheConversationsInTheDatabase() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "datasources.default.url", "jdbc:h2:mem:memory;DB_CLOSE_DELAY=-1",
            "datasources.default.username", "sa",
            "datasources.default.password", "",
            "datasources.default.driver-class-name", "org.h2.Driver",
            "langchain4j.chat-memory-store.jdbc.default.table-name", "chat_memory",
            "langchain4j.chat-memory-store.jdbc.default.auto-create-table", true))) {

            ChatMemoryProvider provider = context.getBean(ChatMemoryProvider.class);
            provider.get("conversation").add(UserMessage.from("Hello"));

            SQLChatMemoryStore store = context.getBean(SQLChatMemoryStore.class);
            assertEquals(1, store.getMessages("conversation").size());
            assertEquals("Hello", ((UserMessage) store.getMessages("conversation").getFirst()).singleText());
            assertInstanceOf(SQLChatMemoryStore.class, context.getBean(ChatMemoryStore.class, io.micronaut.inject.qualifiers.Qualifiers.byName("default")));
        }
    }
}
