package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative LoopAgent workflow wiring through @AgenticService.
 */
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoopingPlannerAgentTest implements OllamaTestPropertyProvider {

    @Test
    void testDeclarativeLoop(LoopingPlannerAgent agent) {
        String result = agent.translatesInLoop("music and cooking");
        assertFalse(result.isEmpty());
    }
}
