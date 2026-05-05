package example.micronaut.agentic;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Validates declarative ConditionalAgent workflow wiring through @AgenticService.
 */
@MicronautTest(startApplication = false, environments = "agentic-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConditionalRouterAgentTest {

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
