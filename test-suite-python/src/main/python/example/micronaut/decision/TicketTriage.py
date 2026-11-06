from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.service import V
from dev.langchain4j.service.decision import Choice, Decide
from micronaut.langchain4j.annotation import DecisionService

from .Team import Team


@DecisionService  # <1>
class TicketTriage(ABC):

    @Decide("Is this message spam?")
    @abstractmethod
    def is_spam(self, message: Annotated[str, V("message")]) -> bool:  # <2>
        ...

    @Decide("Which team should handle this ticket?")
    @abstractmethod
    def route(self, ticket: Annotated[str, V("ticket")]) -> Team:  # <3>
        ...

    @Decide("Which team should handle this ticket?")
    @abstractmethod
    def route_with_probabilities(self, ticket: Annotated[str, V("ticket")]) -> Choice[Team]:  # <4>
        ...
