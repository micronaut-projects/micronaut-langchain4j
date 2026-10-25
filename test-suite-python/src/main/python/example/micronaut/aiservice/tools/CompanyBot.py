from abc import ABC, abstractmethod

from micronaut.langchain4j.annotation import AiService

from .LegalDocumentTools import LegalDocumentTools


@AiService(tools=[LegalDocumentTools])
class CompanyBot(ABC):
    @abstractmethod
    def ask(self, question: str) -> str:
        ...
