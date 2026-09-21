from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.tools.CompanyBot import CompanyBot


@Property(name="langchain4j.ollama.model-name", value="qwen2.5:0.5b")
@MicronautTest(startApplication=False, environments=["ollama"])
class OllamaToolTest:
    bot: Annotated[CompanyBot, Inject]

    @Test
    def test_inject_tools(self):
        assert self.bot is not None
        response = self.bot.ask("When was the PRIVACY document updated?")
        assert "2013" in response, response
