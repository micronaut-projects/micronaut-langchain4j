package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative ConditionalAgent workflow wiring through @AgenticService.
 */
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConditionalRouterAgentTest implements OllamaTestPropertyProvider {

    @Test
    void testFoodBranch(ConditionalRouterAgent agent) {
        String response = agent.route("cooking and recipes");
        System.out.println("conditional (food) response = " + response);
        assertFalse(response.isEmpty());
    }

    @Test
    void testTravelBranch(ConditionalRouterAgent agent) {
        String response = agent.route("hiking and sightseeing");
        System.out.println("conditional (travel) response = " + response);
        assertFalse(response.isEmpty());
    }
}
