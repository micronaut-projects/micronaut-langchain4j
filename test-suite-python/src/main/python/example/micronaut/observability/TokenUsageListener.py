from dev.langchain4j.model.chat.listener import ChatModelListener, ChatModelResponseContext
from jakarta.inject import Singleton
from org.slf4j import LoggerFactory

LOG = LoggerFactory.getLogger("example.micronaut.observability.TokenUsageListener")


@Singleton  # <1>
class TokenUsageListener(ChatModelListener):

    def onResponse(self, response_context: ChatModelResponseContext) -> None:  # <2>
        token_usage = response_context.chatResponse().tokenUsage()
        LOG.info("Tokens used: {}", token_usage)
