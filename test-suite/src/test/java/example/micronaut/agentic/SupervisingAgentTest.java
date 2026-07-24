package example.micronaut.agentic;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative sequence workflow wiring through @AgenticService.
 */
@MicronautTest(startApplication = false, environments = "agentic-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupervisingAgentTest {

    @Test
    void testDeclarativeSequence(SupervisingAgent agent) {
        String plan = agent.plan("outdoor dinner and chill music");
        System.out.println("supervised plan = " + plan);
        assertFalse(plan.isEmpty());
    }
}
