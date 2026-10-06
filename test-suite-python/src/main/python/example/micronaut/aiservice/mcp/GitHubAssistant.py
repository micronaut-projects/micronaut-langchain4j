from abc import ABC, abstractmethod

from micronaut.langchain4j.annotation import AiService


@AiService(mcpClients=["github"])  # <1>
class GitHubAssistant(ABC):
    @abstractmethod
    def ask(self, question: str) -> str:
        ...
