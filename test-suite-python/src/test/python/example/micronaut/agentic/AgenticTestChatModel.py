from dev.langchain4j.data.message import AiMessage
from dev.langchain4j.model.chat import ChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse
from jakarta.inject import Singleton
from micronaut.context.annotation import Primary, Requires

RESPONSE = "agentic test response"


# Mock chat model of the `agentic-test` environment, like in the Java suite: the `chat(...)` convenience methods
# of the interface are its default methods, which the Python implementation inherits.
@Singleton
@Primary
@Requires(env="agentic-test")
class AgenticTestChatModel(ChatModel):

    def doChat(self, chat_request: ChatRequest) -> ChatResponse:
        return ChatResponse.builder().aiMessage(AiMessage.from_(RESPONSE)).build()
