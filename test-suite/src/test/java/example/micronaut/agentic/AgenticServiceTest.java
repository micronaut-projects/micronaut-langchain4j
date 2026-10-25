package example.micronaut.agentic;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest(startApplication = false, environments = "agentic-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgenticServiceTest {

    @Test
    void testAgenticGreeter(GreeterAgent agent) {
        String result = agent.greet("John");
        assertNotNull(result);
    }
}
