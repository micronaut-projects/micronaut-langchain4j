from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.service import UserMessage, V
from micronaut.langchain4j.agentic.annotation import AgenticService


# Minimal typed Agentic service used by the test-suite to validate Micronaut integration.
@AgenticService
class GreeterAgent(ABC):

    @UserMessage("Say hello to {{name}}")
    @Agent(description="Greets a person by name")
    @abstractmethod
    def greet(self, name: Annotated[str, V("name")]) -> str:
        ...
