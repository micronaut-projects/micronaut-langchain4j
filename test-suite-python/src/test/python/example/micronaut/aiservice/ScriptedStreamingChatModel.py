from dev.langchain4j.data.message import AiMessage
from dev.langchain4j.model.chat import StreamingChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse, StreamingChatResponseHandler
from jakarta.inject import Singleton
from micronaut.context.annotation import Primary, Requires


# Streaming chat model of the `scripted-test` environment: it streams "micro" and "naut".
@Singleton
@Primary
@Requires(env="scripted-test")
class ScriptedStreamingChatModel(StreamingChatModel):

    def doChat(self, chat_request: ChatRequest, handler: StreamingChatResponseHandler) -> None:
        handler.onPartialResponse("micro")
        handler.onPartialResponse("naut")
        handler.onCompleteResponse(ChatResponse.builder().aiMessage(AiMessage.from_("micronaut")).build())
