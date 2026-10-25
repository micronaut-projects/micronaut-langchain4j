package example.micronaut

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.TestInstance
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InMemoryAssistantWithMemoryTest  extends AssistantWithMemoryTest {
}
