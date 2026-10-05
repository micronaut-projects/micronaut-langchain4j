from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.service import UserMessage, V
from micronaut.context.annotation import Requires
from micronaut.langchain4j.agentic.annotation import AgenticService

from example.structured.TalkRecommendation import TalkRecommendation


# Agent returning a type annotated with @JsonSchema: its chat model receives the generated schema.
@AgenticService
@Requires(env="structured-output")
class TalkPickerAgent(ABC):

    @UserMessage("Recommend a talk about {{interests}}")
    @Agent(description="Recommends a conference talk")
    @abstractmethod
    def pick(self, interests: Annotated[str, V("interests")]) -> TalkRecommendation:
        ...
