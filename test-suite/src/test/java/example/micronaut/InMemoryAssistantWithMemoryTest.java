package example.micronaut;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

@Property(name = "langchain4j.store-memory-chat.neo4j.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.store-memory-chat.cassandra.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.store-memory-chat.redis.enabled", value = StringUtils.FALSE)
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InMemoryAssistantWithMemoryTest  extends AssistantWithMemoryTest {
}
