package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.testutils.Neo4jUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Property(name = "langchain4j.chat-memory-store.redis.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.cassandra.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.inmemory.enabled", value = StringUtils.FALSE)
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Neo4jAssistantWithMemoryTest extends AssistantWithMemoryTest {

    @Override
    @NonNull
    public Map<String, String> getProperties() {
        Map<String, String> result = super.getProperties();
        try {
            Map<String, Object> props = Neo4jUtils.getProperties();
            for (String k : props.keySet()) {
                result.put(k, props.get(k).toString());
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not set Neo4j properties", e);
        }
        return result;
    }
}
