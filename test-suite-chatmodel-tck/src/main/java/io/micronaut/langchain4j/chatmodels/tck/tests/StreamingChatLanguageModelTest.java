package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.chatmodels.tck.SuiteCondition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Requires(condition = SuiteCondition.class)
@MicronautTest(startApplication = false)
class StreamingChatLanguageModelTest {

    @Inject
    BeanContext beanContext;

    @Test
    void testLanguageModel() {
        assertTrue(beanContext.containsBean(StreamingChatModel.class), "A bean of type StreamingChatLanguageModel should be present");

        StreamingChatModel streamingChatLanguageModel = beanContext.getBean(StreamingChatModel.class);
        StringBuffer sb = new StringBuffer();
        streamingChatLanguageModel.chat("Tell me a joke about Java?", new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String partialResponse) {
                sb.append(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                // no-op
            }

            @Override
            public void onError(Throwable throwable) {
                // no-op
            }
        });
        await().until(() -> sb.toString().contains("Java"));
    }
}
