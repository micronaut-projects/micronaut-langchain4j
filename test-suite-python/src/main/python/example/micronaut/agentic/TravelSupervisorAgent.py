from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic.declarative import SupervisorAgent
from dev.langchain4j.agentic.supervisor import SupervisorResponseStrategy
from dev.langchain4j.service import V
from micronaut.langchain4j.agentic.annotation import AgenticService

from .RecipeAdvisorAgent import RecipeAdvisorAgent
from .TravelRecommenderAgent import TravelRecommenderAgent


# A supervisor agent: the chat model plans which sub-agent to invoke next until the request is addressed.
@AgenticService
class TravelSupervisorAgent(ABC):

    @SupervisorAgent(
        subAgents=[
            TravelRecommenderAgent,
            RecipeAdvisorAgent
        ],
        responseStrategy=SupervisorResponseStrategy.SUMMARY,
        maxAgentsInvocations=5
    )
    @abstractmethod
    def organize(self, request: Annotated[str, V("request")]) -> str:
        ...
