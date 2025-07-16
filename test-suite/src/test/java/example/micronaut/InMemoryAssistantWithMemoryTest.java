package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

@Property(name = "langchain4j.chat-memory-store.neo4j.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.cassandra.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.chat-memory-store.redis.enabled", value = StringUtils.FALSE)
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InMemoryAssistantWithMemoryTest  extends AssistantWithMemoryTest {
}
