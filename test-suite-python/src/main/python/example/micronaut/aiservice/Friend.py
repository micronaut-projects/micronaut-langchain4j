from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from micronaut.langchain4j.annotation import AiService


@AiService  # <1>
class Friend(ABC):

    @SystemMessage("You are a good friend of mine. Answer using slang.")  # <2>
    @abstractmethod
    def chat(self, user_message: str) -> str:
        ...
