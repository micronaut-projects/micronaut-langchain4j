package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.ConfigurationException;
import org.jspecify.annotations.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.testutils.RedisUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
@Property(name = "langchain4j.chat-memory-store.neo4j.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.cassandra.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.inmemory.enabled", value = StringUtils.FALSE)
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisAssistantWithMemoryTest extends AssistantWithMemoryTest {

    @Override
    public @NonNull Map<String, String> getProperties() {
        Map<String, String> result = super.getProperties();
        try {
            Map<String, Object> redisProperties = RedisUtils.getProperties();
            for (String k : redisProperties.keySet()) {
                result.put(k, redisProperties.get(k).toString());
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not get Redis properties", e);
        }
        return result;
    }
}
