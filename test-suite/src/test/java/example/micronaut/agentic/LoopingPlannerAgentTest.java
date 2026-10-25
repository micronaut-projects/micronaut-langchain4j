package example.micronaut.agentic;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative LoopAgent workflow wiring through @AgenticService.
 */
@MicronautTest(startApplication = false, environments = "agentic-test")
@Property(name = "spec.name", value = "LoopingPlannerAgentTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoopingPlannerAgentTest {

    @Test
    void testDeclarativeLoop(LoopingPlannerAgent agent) {
        String result = agent.translatesInLoop("music and cooking");
        assertFalse(result.isEmpty());
    }
}
