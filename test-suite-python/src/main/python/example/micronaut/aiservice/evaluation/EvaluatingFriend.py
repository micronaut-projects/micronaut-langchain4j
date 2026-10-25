from abc import ABC, abstractmethod

from dev.langchain4j.service import Result, SystemMessage
from micronaut.context.annotation import Requires
from micronaut.langchain4j.annotation import AiService


@Requires(property="spec.name", value="AiServiceEvaluationExample")
@AiService
class EvaluatingFriend(ABC):

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    @abstractmethod
    def chat(self, user_message: str) -> Result[str]:
        ...
