from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.service import UserMessage, V
from micronaut.langchain4j.agentic.annotation import AgenticService


# Aggregates intermediate results into a final "plan".
# Derived agent id: "plan-synthesizer".
@AgenticService(outputKey="plan")
class PlanSynthesizerAgent(ABC):

    @UserMessage("""
        You are a helpful planner.
        Create a concise plan for an enjoyable evening in {{country}} that features the recipe "{{recipe}}".
        Respond with a single sentence plan with concrete, practical steps.
        """)
    @Agent(description="Synthesizes a final evening plan from intermediate agent outputs")
    @abstractmethod
    def synthesize(self, country: Annotated[str, V("country")], recipe: Annotated[str, V("recipe")]) -> str:
        ...
