package example.micronaut.aiservice.tools;

import io.micronaut.context.annotation.Property;
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.langchain4j.testutils.OllamaUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Testcontainers;

@Property(name = "langchain4j.ollama.model-name", value = OllamaUtils.TOOL_MODEL_NAME)
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
class OllamaToolTest implements OllamaTestPropertyProvider {
    @Test
    void testInjectTools(CompanyBot bot) {
        assertNotNull(bot);
        String response = bot.ask("When was the PRIVACY document updated?");
        assertTrue(response.contains("2013"), response);
    }
}
