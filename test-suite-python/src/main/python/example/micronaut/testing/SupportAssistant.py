from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from micronaut.langchain4j.annotation import AiService


@AiService
class SupportAssistant(ABC):

    @SystemMessage("You answer questions about Micronaut")
    @abstractmethod
    def answer(self, question: str) -> str:
        ...
