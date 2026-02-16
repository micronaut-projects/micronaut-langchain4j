package io.micronaut.langchain4j.openai;

import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.BeanContext;import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.open-ai.api-key", value = "blah")
@Property(name = "langchain4j.open-ai.organization-id", value = "blah")
public class OpenAiChatModelBuilderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void openAiChatModelBuilder() {
        beanContext.containsBean(OpenAiChatModel.class);
        beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class);
    }

}
