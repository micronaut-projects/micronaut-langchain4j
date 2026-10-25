from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.Friend import Friend
from example.micronaut.aiservice.tools.CompanyBot import CompanyBot


# The AI services and tools written in Python work without @AllowsReflection or the allow-reflection compiler
# option: the system message of the AI service and the @Tool method of the tool bean reach the chat model.
@MicronautTest(startApplication=False, environments=["scripted-test"])
class ScriptedAiServiceTest:
    friend: Annotated[Friend, Inject]
    bot: Annotated[CompanyBot, Inject]

    @Test
    def test_system_message(self):
        assert self.friend.chat("Hello") == "system:You are a good friend of mine. Answer using slang."

    @Test
    def test_tool(self):
        response = self.bot.ask("When was the PRIVACY document updated?")
        # the LocalDate returned by the tool is sent to the model as JSON
        assert response == 'tool:"2013-03-09"', response
