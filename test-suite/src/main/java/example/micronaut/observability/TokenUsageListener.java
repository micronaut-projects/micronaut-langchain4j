package example.micronaut.observability;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton // <1>
public class TokenUsageListener implements ChatModelListener {

    private static final Logger LOG = LoggerFactory.getLogger(TokenUsageListener.class);

    @Override
    public void onResponse(ChatModelResponseContext responseContext) { // <2>
        TokenUsage tokenUsage = responseContext.chatResponse().tokenUsage();
        LOG.info("Tokens used: {}", tokenUsage);
    }
}
