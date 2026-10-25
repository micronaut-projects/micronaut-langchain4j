from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from micronaut.langchain4j.annotation import AiService

from example.structured.TalkRecommendation import TalkRecommendation


@AiService
class TalkAdvisor(ABC):

    @SystemMessage("You recommend conference talks to attendees.")
    @abstractmethod
    def recommend(self, interests: str) -> TalkRecommendation:  # <1>
        ...

    @SystemMessage("You recommend conference talks to attendees.")
    @abstractmethod
    def shortlist(self, interests: str) -> list[TalkRecommendation]:  # <2>
        ...
