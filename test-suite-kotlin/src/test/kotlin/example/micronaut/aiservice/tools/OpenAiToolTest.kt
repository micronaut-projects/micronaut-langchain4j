package example.micronaut.aiservice.tools

import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.ollama.enabled", value = StringUtils.FALSE)
@Property(name = "langchain4j.open-ai.enabled", value = StringUtils.TRUE)
@Property(name = "langchain4j.open-ai.model-name", value = "gpt-4.1")
@EnabledIfEnvironmentVariable(named = "LANGCHAIN4J_OPEN_AI_API_KEY", matches = ".+")
internal class OpenAiToolTest {
    @Test
    fun testInjectTools(bot: CompanyBot) {
        Assertions.assertNotNull(bot)
        val response = bot.ask("When was the PRIVACY document updated?")
        Assertions.assertTrue(response.contains("2013"), response)
    }
}
