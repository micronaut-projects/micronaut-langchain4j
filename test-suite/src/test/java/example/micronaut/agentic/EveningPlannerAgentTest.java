package example.micronaut.agentic;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative sequence workflow wiring through @AgenticService.
 * Ensures Micronaut DI correctly builds the agentic system and executes the generated plan.
 */
@MicronautTest(startApplication = false, environments = "agentic-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EveningPlannerAgentTest {

    @Test
    void testDeclarativeSequence(EveningPlannerAgent agent) {
        String plan = agent.plan("music and cooking");
        System.out.println("plan = " + plan);
        assertFalse(plan.isEmpty());
    }
}
