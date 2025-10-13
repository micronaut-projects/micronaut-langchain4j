package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative SupervisorAgent workflow wiring through @AgenticService.
 * Ensures Micronaut DI correctly builds the agentic system and executes the supervisor plan.
 */
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EveningPlannerAgentTest implements OllamaTestPropertyProvider {

    @Test
    void testDeclarativeSupervisor(EveningPlannerAgent agent) {
        String plan = agent.plan("music and cooking");
        System.out.println("plan = " + plan);
        assertFalse(plan.isEmpty());
    }
}
