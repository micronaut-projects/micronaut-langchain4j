from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from org.junit.jupiter.api.condition import EnabledIfEnvironmentVariable

from example.micronaut.aiservice.tools.CompanyBot import CompanyBot


@MicronautTest(startApplication=False)
@Property(name="langchain4j.ollama.enabled", value="false")
@Property(name="langchain4j.open-ai.enabled", value="true")
@Property(name="langchain4j.open-ai.model-name", value="gpt-4.1")
@EnabledIfEnvironmentVariable(
    named="LANGCHAIN4J_OPEN_AI_API_KEY",
    matches=".+"
)
class OpenAiToolTest:
    bot: Annotated[CompanyBot, Inject]

    @Test
    def test_inject_tools(self):
        assert self.bot is not None
        response = self.bot.ask("When was the PRIVACY document updated?")
        assert "2013" in response, response
