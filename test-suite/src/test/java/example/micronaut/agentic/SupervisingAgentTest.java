package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative SupervisorAgent workflow wiring through @AgenticService.
 */
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SupervisingAgentTest implements OllamaTestPropertyProvider {

    @Test
    void testDeclarativeSupervisor(SupervisingAgent agent) {
        String plan = agent.plan("outdoor dinner and chill music");
        System.out.println("supervised plan = " + plan);
        assertFalse(plan.isEmpty());
    }
}
