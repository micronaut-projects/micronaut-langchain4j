package example.micronaut.agentic

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Validates declarative LoopAgent workflow wiring through @AgenticService.
 */
@MicronautTest(startApplication = false, environments = ["agentic-test"])
@Property(name = "spec.name", value = "LoopingPlannerAgentTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class LoopingPlannerAgentTest {

    @Test
    fun testDeclarativeLoop(agent: LoopingPlannerAgent) {
        val result = agent.translatesInLoop("music and cooking")
        assertFalse(result.isEmpty())
    }
}
