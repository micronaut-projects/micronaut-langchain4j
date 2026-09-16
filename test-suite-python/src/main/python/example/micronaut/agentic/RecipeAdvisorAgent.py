from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.service import UserMessage, V
from micronaut.langchain4j.agentic.annotation import AgenticService


# Suggests a popular recipe for a given country.
# Derived agent id: "recipe-advisor".
@AgenticService(outputKey="recipe")
class RecipeAdvisorAgent(ABC):

    @UserMessage("""
        You are a cooking expert.
        Suggest a popular recipe in {{country}}.
        Reply with the country name and the recipe name separated by a minus sign, then the details.
        """)
    @Agent(description="Suggests a popular recipe based on a user request")
    @abstractmethod
    def suggest_recipe(self, country: Annotated[str, V("country")]) -> str:
        ...
