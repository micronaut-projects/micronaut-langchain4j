package example.micronaut.aiservice.tools;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.util.StringUtils;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.open-ai.enabled", value = StringUtils.TRUE)
@Property(name = "langchain4j.open-ai.model-name", value = "gpt-4.1")
@EnabledIfEnvironmentVariable(
    named = "LANGCHAIN4J_OPEN_AI_API_KEY",
    matches = ".+"
)
class OpenAiToolTest {
    @Test
    void testInjectTools(CompanyBot bot) {
        assertNotNull(bot);
        String response = bot.ask("When was the PRIVACY document updated?");
        assertTrue(response.contains("2013"), response);
    }
}
