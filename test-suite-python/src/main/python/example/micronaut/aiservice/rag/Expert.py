from abc import ABC, abstractmethod

from dev.langchain4j.service import Result
from micronaut.context.annotation import Requires
from micronaut.langchain4j.annotation import AiService


@Requires(property="spec.name", value="RagTest")
@AiService  # <1>
class Expert(ABC):
    @abstractmethod
    def ask(self, question: str) -> Result[str]:  # <2>
        ...
