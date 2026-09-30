from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic import Agent
from dev.langchain4j.agentic.declarative import Output, ParallelAgent, ParallelExecutor
from dev.langchain4j.service import UserMessage, V
from jakarta.inject import Inject
from java.util.concurrent import Executor, ForkJoinPool
from micronaut.langchain4j.agentic.annotation import AgenticService
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@AgenticService
class MusicPlanner(ABC):
    @UserMessage("""
        Choose a band which plays music in the {{style}} style.
        Answer with the name of the band only: no details, no explanation.
        """)
    @Agent(outputKey="band")
    @abstractmethod
    def suggest_band(self, style: Annotated[str, V("style")]) -> str:
        ...


@AgenticService
class DinnerPlanner(ABC):
    @UserMessage("""
        Choose a menu for dinner for the following mood: {{mood}}
        Answer with the menu only: no details, no explanations.
        """)
    @Agent(outputKey="menu")
    @abstractmethod
    def suggest_menu(self, mood: Annotated[str, V("mood")]) -> str:
        ...


# Demonstrates declarative ParallelAgent orchestration using Micronaut DI.
@AgenticService(outputKey="plan")
class EveningPlanner(ABC):

    @ParallelAgent(
        subAgents=[
            MusicPlanner,
            DinnerPlanner
        ],
        outputKey="plan",
        name="planEvening"
    )
    @abstractmethod
    def plan(self, style: Annotated[str, V("style")], mood: Annotated[str, V("mood")]) -> str:
        ...

    # Use a shared executor for parallel execution
    @ParallelExecutor
    @staticmethod
    def executor() -> Executor:
        return ForkJoinPool.commonPool()

    # Aggregate the parallel outputs into a single "plan" string
    @Output
    @staticmethod
    def aggregate(band: Annotated[str, V("band")], menu: Annotated[str, V("menu")]) -> str:
        c = "" if band is None else band.strip()
        r = "" if menu is None else menu.strip()
        if c == "" and r == "":
            return ""
        if c == "":
            return f"Play: {r}"
        if r == "":
            return f"Menu: {c}"
        return f"Play: {c} | Menu: {r}"


# Validates declarative ParallelAgent workflow wiring through @AgenticService.
# Ensures Micronaut DI correctly builds the agentic system and executes the parallel plan.
@MicronautTest(startApplication=False, environments=["agentic-test"])
class ParallelPlanningAgentTest:
    agent: Annotated[EveningPlanner, Inject]

    @Test
    def test_declarative_parallel(self):
        plan = self.agent.plan("jazz", "romantic")
        print(f"parallel plan = {plan}")
        assert plan != ""
