from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from micronaut.langchain4j.annotation import AiService


@AiService
class Concierge(ABC):

    @SystemMessage("You are the concierge of a hotel.")
    @abstractmethod
    def ask(self, question: str) -> str:
        ...
