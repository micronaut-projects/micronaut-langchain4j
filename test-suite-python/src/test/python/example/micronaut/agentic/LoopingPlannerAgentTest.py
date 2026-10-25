from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.agentic.LoopingPlannerAgent import LoopingPlannerAgent


# Validates declarative LoopAgent workflow wiring through @AgenticService.
@MicronautTest(startApplication=False, environments=["agentic-test"])
@Property(name="spec.name", value="LoopingPlannerAgentTest")
class LoopingPlannerAgentTest:
    agent: Annotated[LoopingPlannerAgent, Inject]

    @Test
    def test_declarative_loop(self):
        result = self.agent.translates_in_loop("music and cooking")
        assert result != ""
