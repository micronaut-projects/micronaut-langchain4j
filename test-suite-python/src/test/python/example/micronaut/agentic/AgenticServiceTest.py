from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.agentic.GreeterAgent import GreeterAgent


@MicronautTest(startApplication=False, environments=["agentic-test"])
class AgenticServiceTest:
    agent: Annotated[GreeterAgent, Inject]

    @Test
    def test_agentic_greeter(self):
        result = self.agent.greet("John")
        assert result is not None
