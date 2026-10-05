from abc import ABC, abstractmethod

from dev.langchain4j.service.guardrail import InputGuardrails
from micronaut.langchain4j.annotation import AiService

from .PromptInjectionGuard import PromptInjectionGuard


@AiService
class GuardedAssistant(ABC):

    @InputGuardrails([PromptInjectionGuard])  # <1>
    @abstractmethod
    def chat(self, user_message: str) -> str:
        ...
