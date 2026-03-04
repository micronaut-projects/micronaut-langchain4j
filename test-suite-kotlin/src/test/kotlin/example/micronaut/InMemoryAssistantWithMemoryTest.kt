package example.micronaut

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.testcontainers.junit.jupiter.Testcontainers

@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InMemoryAssistantWithMemoryTest : AssistantWithMemoryTest()
