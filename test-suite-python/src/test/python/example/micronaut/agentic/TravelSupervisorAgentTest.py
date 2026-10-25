from typing import Annotated

from dev.langchain4j.data.message import AiMessage, SystemMessage
from dev.langchain4j.model.chat import ChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse
from jakarta.inject import Inject, Singleton
from micronaut.context.annotation import Primary, Property, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.agentic.TravelSupervisorAgent import TravelSupervisorAgent


@Property(name="langchain4j.ollama.enabled", value="false")
@MicronautTest(startApplication=False, environments=["supervisor-test"])
class TravelSupervisorAgentTest:
    supervisor: Annotated[TravelSupervisorAgent, Inject]

    @Test
    def supervisor_invokes_the_sub_agents(self):
        assert self.supervisor.organize("I love jazz, plan my holidays") == "Visit Italy and cook a pizza"


def _answer(text: str) -> ChatResponse:
    return ChatResponse.builder().aiMessage(AiMessage.from_(text)).build()


# Plays the planner of the supervisor: recommends a country, then a recipe, then answers.
@Singleton
@Primary
@Requires(env="supervisor-test")
class ScriptedPlannerChatModel(ChatModel):

    def doChat(self, request: ChatRequest) -> ChatResponse:
        messages = request.messages()
        user = messages.get(messages.size() - 1).singleText()
        planner = any(isinstance(message, SystemMessage) and "planner" in message.text() for message in messages)
        if not planner:
            return _answer("Italy" if "travel expert" in user else "Italy - Pizza")
        if "'Italy - Pizza'" in user:
            return _answer('{"agentName": "done", "arguments": {"response": "Visit Italy and cook a pizza"}}')
        if "'Italy'" in user:
            return _answer('{"agentName": "suggest_recipe", "arguments": {"country": "Italy"}}')
        return _answer('{"agentName": "recommend_country", "arguments": {"topic": "jazz"}}')
