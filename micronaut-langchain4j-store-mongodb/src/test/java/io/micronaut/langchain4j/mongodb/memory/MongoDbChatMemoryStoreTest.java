package io.micronaut.langchain4j.mongodb.memory;

import dev.langchain4j.community.store.memory.chat.mongodb.MongoDbChatMemoryStore;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The MongoDB chat memory store against a MongoDB server, and the default chat memory provider that uses it.
 */
@Testcontainers(disabledWithoutDocker = true)
class MongoDbChatMemoryStoreTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:8.3.8");

    @Test
    void persistsTheConversationsInMongoDb() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "mongodb.servers.default.uri", MONGO.getReplicaSetUrl(),
            "langchain4j.chat-memory-store.mongodb.default.database-name", "chat",
            "langchain4j.chat-memory-store.mongodb.default.collection-name", "memory"))) {

            ChatMemory memory = context.getBean(ChatMemoryProvider.class).get("conversation");
            memory.add(UserMessage.from("Hello"));
            memory.add(AiMessage.from("Hi!"));

            MongoDbChatMemoryStore store = context.getBean(MongoDbChatMemoryStore.class);
            assertEquals(2, store.getMessages("conversation").size());
        }
    }
}
