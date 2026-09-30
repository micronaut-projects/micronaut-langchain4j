from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.agentic.declarative import LoopAgent
from dev.langchain4j.agentic.workflow import LoopAgentService
from dev.langchain4j.service import UserMessage, V
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener
from micronaut.langchain4j.agentic.annotation import AgenticService


class TranslatorAgent(ABC):
    @UserMessage("""
        You are a translator.
        If the text is in English, translate to French.
        If the text is in French, translate to German.
        If the text is in German, translate to Spanish.
        Translate this: "{{text}}". Answer with the translation only, no explanations, no details.
        """)
    @Agent(outputKey="text")
    @abstractmethod
    def translate(self, text: Annotated[str, V("text")]) -> str:
        ...


# Demonstrates declarative LoopAgent orchestration using Micronaut DI.
@AgenticService(outputKey="translation")
class LoopingPlannerAgent(ABC):

    @LoopAgent(
        subAgents=[
            TranslatorAgent
        ],
        outputKey="text",
        maxIterations=3
    )
    @abstractmethod
    def translates_in_loop(self, text: Annotated[str, V("text")]) -> str:
        ...


@Singleton
@Requires(property="spec.name", value="LoopingPlannerAgentTest")
class LoopBuilderListener(BeanCreatedEventListener[LoopAgentService]):

    def onCreated(self, event: BeanCreatedEvent[LoopAgentService]) -> LoopAgentService:
        builder = event.getBean()
        builder.exitCondition(lambda scope, idx: idx == 3)
        return builder
