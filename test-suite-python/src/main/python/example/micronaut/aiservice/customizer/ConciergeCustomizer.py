from java.util.function import UnaryOperator
from jakarta.inject import Singleton
from micronaut.langchain4j.aiservices import AiServiceCreationContext, AiServiceCustomizer

from .Concierge import Concierge


class ShortAnswers(UnaryOperator):
    def apply(self, system_message: str) -> str:
        return system_message + " Keep your answers short."


@Singleton
class ConciergeCustomizer(AiServiceCustomizer[Concierge]):  # <1>

    def customize(self, creation_context: AiServiceCreationContext[Concierge]) -> None:
        creation_context.aiServices().systemMessageTransformer(ShortAnswers())  # <2>
