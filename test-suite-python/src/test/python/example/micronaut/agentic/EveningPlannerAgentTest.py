from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.agentic.EveningPlannerAgent import EveningPlannerAgent


# Validates declarative sequence workflow wiring through @AgenticService.
# Ensures Micronaut DI correctly builds the agentic system and executes the generated plan.
@MicronautTest(startApplication=False, environments=["agentic-test"])
class EveningPlannerAgentTest:
    agent: Annotated[EveningPlannerAgent, Inject]

    @Test
    def test_declarative_sequence(self):
        plan = self.agent.plan("music and cooking")
        print(f"plan = {plan}")
        assert plan != ""
