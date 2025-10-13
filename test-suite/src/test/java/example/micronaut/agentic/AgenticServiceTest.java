package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AgenticServiceTest implements OllamaTestPropertyProvider {

    @Test
    void testAgenticGreeter(GreeterAgent agent) {
        String result = agent.greet("John");
        assertNotNull(result);
    }
}
