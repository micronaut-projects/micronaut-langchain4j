from abc import ABC, abstractmethod

from dev.langchain4j.service import Result, SystemMessage
from micronaut.context.annotation import Requires
from micronaut.langchain4j.annotation import AiService

from .ReleaseTools import ReleaseTools


@Requires(property="spec.name", value="KnowledgeAssistantTest")
@AiService(value="knowledge", tools=[ReleaseTools])  # <1>
class KnowledgeAssistant(ABC):

    @SystemMessage("You answer questions about Micronaut from the documents you are given")  # <2>
    @abstractmethod
    def ask(self, question: str) -> Result[str]:  # <3>
        ...
