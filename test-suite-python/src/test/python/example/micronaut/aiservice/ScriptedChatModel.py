from dev.langchain4j.agent.tool import ToolExecutionRequest
from dev.langchain4j.data.message import AiMessage, SystemMessage, ToolExecutionResultMessage
from dev.langchain4j.model.chat import ChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse
from jakarta.inject import Singleton
from java.util import List
from micronaut.context.annotation import Primary, Requires


# Chat model of the `scripted-test` environment: it asks for the first tool it is offered, then answers with the
# result of the tool, or, without tools, with the system message it received. The answers show what LangChain4j
# read from the Python AI services and tools, without a model server.
@Singleton
@Primary
@Requires(env="scripted-test")
class ScriptedChatModel(ChatModel):

    def doChat(self, chat_request: ChatRequest) -> ChatResponse:
        messages = chat_request.messages()
        last = messages.get(messages.size() - 1)
        if isinstance(last, ToolExecutionResultMessage):
            return self._answer(f"tool:{last.text()}")
        tools = chat_request.toolSpecifications()
        if tools is not None and not tools.isEmpty():
            request = ToolExecutionRequest.builder().id("1").name(tools.get(0).name()).arguments("{}").build()
            return ChatResponse.builder().aiMessage(AiMessage.builder().toolExecutionRequests(List.of(request)).build()).build()
        system = [message.text() for message in messages if isinstance(message, SystemMessage)]
        return self._answer(f"system:{system[0] if system else ''}")

    def _answer(self, text: str) -> ChatResponse:
        return ChatResponse.builder().aiMessage(AiMessage.from_(text)).build()
