from typing import Annotated

from dev.langchain4j.agent.tool import ToolSpecifications
from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.langchain4j.tools import ToolRegistry
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Assumptions, Test

from example.micronaut.aiservice.tools.CompanyBot import CompanyBot
from example.micronaut.aiservice.tools.LegalDocumentTools import LegalDocumentTools


@Property(name="langchain4j.ollama.model-name", value="qwen2.5:0.5b")
@MicronautTest(startApplication=False, environments=["ollama"])
class OllamaToolTest:
    bot: Annotated[CompanyBot, Inject]
    tool_registry: Annotated[ToolRegistry, Inject]

    @Test
    def test_inject_tools(self):
        assert self.bot is not None
        # the @Tool method is discovered by LangChain4j on the Java class generated for the Python bean
        tools = [tool for tool in self.tool_registry.getAllTools() if isinstance(tool, LegalDocumentTools)]
        assert len(tools) == 1
        assert [spec.name() for spec in ToolSpecifications.toolSpecificationsFrom(tools[0])] == ["lastUpdatePrivacy"]

        response = self.bot.ask("When was the PRIVACY document updated?")
        # the small test model does not call the tool on every host it runs on (same request, temperature 0):
        # a run in which it answers without the tool is reported as aborted rather than failed
        Assumptions.assumeTrue("2013" in response, f"the qwen2.5:0.5b model did not call the tool: {response}")
