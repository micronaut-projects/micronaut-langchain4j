from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic.declarative import SequenceAgent
from dev.langchain4j.service import V
from micronaut.langchain4j.agentic.annotation import AgenticService

from .PlanSynthesizerAgent import PlanSynthesizerAgent
from .RecipeAdvisorAgent import RecipeAdvisorAgent
from .TravelRecommenderAgent import TravelRecommenderAgent


# Declarative sequence workflow inspired by the "EveningPlannerAgent" example.
# This agent coordinates sub-agents and produces a final "plan" output.
@AgenticService(outputKey="plan")
class EveningPlannerAgent(ABC):

    # Declarative sequence workflow definition (no method body needed)
    @SequenceAgent(
        subAgents=[
            TravelRecommenderAgent,
            RecipeAdvisorAgent,
            PlanSynthesizerAgent
        ],
        outputKey="plan",
        name="planEvening"
    )
    @abstractmethod
    def plan(self, topic: Annotated[str, V("topic")]) -> str:
        ...
