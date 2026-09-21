package example.micronaut.agentic

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

/**
 * Validates declarative sequence workflow wiring through @AgenticService.
 * Ensures Micronaut DI correctly builds the agentic system and executes the generated plan.
 */
@MicronautTest(startApplication = false, environments = ["agentic-test"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class EveningPlannerAgentTest {

    @Test
    fun testDeclarativeSequence(agent: EveningPlannerAgent) {
        val plan = agent.plan("music and cooking")
        println("plan = $plan")
        assertFalse(plan.isEmpty())
    }
}
