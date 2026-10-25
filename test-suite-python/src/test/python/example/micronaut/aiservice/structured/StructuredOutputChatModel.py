from dev.langchain4j.data.message import AiMessage
from dev.langchain4j.model.chat import Capability, ChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse
from jakarta.inject import Singleton
from java.util import Set
from micronaut.context.annotation import Primary, Requires

TALK = '{"title": "Structured outputs", "reason": "You like LLMs", "score": 5}'


# Chat model of the `structured-output` environment: it records the request and answers with JSON matching the
# requested schema.
@Singleton
@Primary
@Requires(env="structured-output")
class StructuredOutputChatModel(ChatModel):

    def __init__(self):
        self.requests = []

    def doChat(self, chat_request: ChatRequest) -> ChatResponse:
        self.requests.append(chat_request)
        name = str(chat_request.responseFormat().jsonSchema().name())
        answer = '{"values": [' + TALK + ']}' if name.startswith("List_of_") else TALK
        return ChatResponse.builder().aiMessage(AiMessage.from_(answer)).build()

    def supportedCapabilities(self) -> Set:
        return Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA)

    def last_request(self) -> ChatRequest:
        return self.requests[-1]
