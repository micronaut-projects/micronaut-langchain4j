from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from micronaut.langchain4j.annotation import AiService


@AiService(skills="travel")  # <1>
class TravelAgent(ABC):

    @SystemMessage("You are a travel agent")  # <2>
    @abstractmethod
    def chat(self, message: str) -> str:
        ...
