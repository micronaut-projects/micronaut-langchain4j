package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.testutils.CassandraUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Property(name = "langchain4j.store-memory-chat.neo4j.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.store-memory-chat.redis.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.store-memory-chat.inmemory.enabled", value = StringUtils.FALSE)
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
}
