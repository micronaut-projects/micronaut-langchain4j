package example.micronaut.observability

import dev.langchain4j.model.chat.listener.ChatModelListener
import dev.langchain4j.model.chat.listener.ChatModelResponseContext
import dev.langchain4j.model.output.TokenUsage
import groovy.util.logging.Slf4j
import jakarta.inject.Singleton

@Slf4j
@Singleton // <1>
class TokenUsageListener implements ChatModelListener {

    @Override
    void onResponse(ChatModelResponseContext responseContext) { // <2>
        TokenUsage tokenUsage = responseContext.chatResponse().tokenUsage()
        log.info("Tokens used: {}", tokenUsage)
    }
}
