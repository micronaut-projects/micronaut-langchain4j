package example.micronaut;

import dev.langchain4j.store.memory.chat.cassandra.CassandraChatMemoryStore;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.testutils.CassandraUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Property(name = "langchain4j.chat-memory-store.neo4j.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.redis.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.inmemory.enabled", value = StringUtils.FALSE)
@Property(name = "spec.name", value = "CassandraAssistantWithMemoryTest")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CassandraAssistantWithMemoryTest extends AssistantWithMemoryTest {

    @Override
    public @NonNull Map<String, String> getProperties() {
        Map<String, String> result = super.getProperties();
        try {
            Map<String, Object> props = CassandraUtils.getProperties();
            for (String k : props.keySet()) {
                result.put(k, props.get(k).toString());
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not get Cassandra properties", e);
        }

        return result;
    }

    @Requires(property = "spec.name", value = "CassandraAssistantWithMemoryTest")
    @Singleton
    static class CassandraChatMemoryStoreBeanCreatedEventListener implements BeanCreatedEventListener<CassandraChatMemoryStore> {
        @Override
        public CassandraChatMemoryStore onCreated(@NonNull BeanCreatedEvent<CassandraChatMemoryStore> event) {
            CassandraChatMemoryStore store = event.getBean();
            store.create();
            return store;
        }
    }
}
