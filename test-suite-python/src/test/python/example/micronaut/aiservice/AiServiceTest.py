from typing import Annotated

from dev.langchain4j.model.chat import ChatModel
from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.Friend import Friend


@MicronautTest(startApplication=False, environments=["ollama"])
class AiServiceTest:
    friend: Annotated[Friend, Inject]
    language_model: Annotated[ChatModel, Inject]

    @Test
    def test_ai_service(self):
        result = self.friend.chat("Hello")

        assert result is not None
        assert self.language_model is not None
