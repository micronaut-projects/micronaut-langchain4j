package example.micronaut.aiservice.tools;

import dev.langchain4j.exception.InvalidRequestException;
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Testcontainers;

@DisabledIfEnvironmentVariable(named = "CI", matches = ".*")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OllamaToolTest implements OllamaTestPropertyProvider {
    @Test
    void testInjectTools(CompanyBot bot) {
        assertNotNull(bot);
        InvalidRequestException e = assertThrows(InvalidRequestException.class, () ->
            bot.ask("When was the PRIVACY document updated?")
        );
        assertTrue(e.getMessage().contains("does not support tools"), e.getMessage());
    }
}
