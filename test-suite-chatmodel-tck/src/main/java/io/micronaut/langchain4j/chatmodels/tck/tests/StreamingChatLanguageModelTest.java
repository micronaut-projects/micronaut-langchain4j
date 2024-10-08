package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
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
        assertTrue(beanContext.containsBean(StreamingChatLanguageModel.class), "A bean of type StreamingChatLanguageModel should be present");

        StreamingChatLanguageModel streamingChatLanguageModel = beanContext.getBean(StreamingChatLanguageModel.class);
        StringBuffer sb = new StringBuffer();
        streamingChatLanguageModel.generate("Tell me a joke about Java?", new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String s) {
                sb.append(s);
            }

            @Override
            public void onError(Throwable throwable) {
                // no-op
            }
        });
        await().until(() -> sb.toString().contains("Java"));
    }
}
