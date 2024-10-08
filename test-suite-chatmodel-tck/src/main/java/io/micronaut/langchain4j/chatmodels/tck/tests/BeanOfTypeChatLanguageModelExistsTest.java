package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.chatmodels.tck.SuiteCondition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Requires(condition = SuiteCondition.class)
@MicronautTest(startApplication = false)
public class BeanOfTypeChatLanguageModelExistsTest {
    @Inject
    BeanContext beanContext;

    @Test
    void beanOfTypeChatLanguageModelExists() {
        assertTrue(beanContext.containsBean(ChatLanguageModel.class), "it is possible to inject a bean of type ChatLanguageModel");
    }
}
