package io.micronaut.langchain4j.jdbc.memory;

import dev.langchain4j.community.store.memory.chat.sql.SQLChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The JDBC chat memory store with a PostgreSQL database provided by Micronaut Test Resources.
 */
@Property(name = "datasources.default.db-type", value = "postgres")
@Property(name = "datasources.default.dialect", value = "POSTGRES")
@Property(name = "langchain4j.chat-memory-store.jdbc.default.auto-create-table", value = "true")
@MicronautTest(startApplication = false, transactional = false)
class PostgresChatMemoryStoreTest {

    @Inject
    ChatMemoryProvider provider;

    @Inject
    SQLChatMemoryStore store;

    @Test
    void persistsTheConversationsInPostgres() {
        ChatMemory memory = provider.get("postgres-conversation");
        memory.add(UserMessage.from("Hello"));
        memory.add(AiMessage.from("Hi!"));

        assertEquals(2, store.getMessages("postgres-conversation").size());
        assertEquals("Hi!", ((AiMessage) store.getMessages("postgres-conversation").getLast()).text());
    }
}
