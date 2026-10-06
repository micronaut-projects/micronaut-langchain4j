from abc import ABC, abstractmethod

from micronaut.langchain4j.annotation import AiService


@AiService(toolProviders=["weather"])  # <1>
class WeatherAssistant(ABC):
    @abstractmethod
    def ask(self, question: str) -> str:
        ...
