package example.micronaut.observability

import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton // <1>
class TokenUsageListener : ChatModelListener {

    override fun onResponse(responseContext: ChatModelResponseContext) { // <2>
        val tokenUsage = responseContext.chatResponse().tokenUsage()
        LOG.info("Tokens used: {}", tokenUsage)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(TokenUsageListener::class.java)
    }
}
