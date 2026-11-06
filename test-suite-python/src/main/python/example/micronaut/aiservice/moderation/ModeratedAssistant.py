from abc import ABC, abstractmethod

from dev.langchain4j.service import Moderate
from micronaut.langchain4j.annotation import AiService


@AiService
class ModeratedAssistant(ABC):

    @Moderate  # <1>
    @abstractmethod
    def chat(self, user_message: str) -> str:
        ...
