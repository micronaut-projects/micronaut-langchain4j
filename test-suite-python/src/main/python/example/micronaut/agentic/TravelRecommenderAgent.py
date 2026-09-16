from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.service import UserMessage, V
from micronaut.langchain4j.agentic.annotation import AgenticService


# Recommends a country to visit based on a topic the user cares about.
# Derived agent id: "travel-recommender".
@AgenticService(outputKey="country")
class TravelRecommenderAgent(ABC):

    @UserMessage("""
    You are a travel expert and you know a lot about the culture of each country.
    The user is interested in {{topic}}, what's the best country to visit in order to entertain this?
    Your answer MUST be the country name only: no punctuation, no explanations, just the country name.
    """)
    @Agent(description="Recommends a country to visit for a given user interest")
    @abstractmethod
    def recommend_country(self, topic: Annotated[str, V("topic")]) -> str:
        ...
