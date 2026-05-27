package io.micronaut.langchain4j.openai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.open-ai.api-key", value = "blah")
@Property(name = "langchain4j.open-ai.organization-id", value = "blah")
@Property(name = "langchain4j.open-ai.chat-models.default.model-name", value = "gpt-4.1")
@Property(name = "langchain4j.open-ai.chat-models.pirate.model-name", value = "gpt-4o-mini")
@Property(name = "langchain4j.open-ai.chat-models.poet.model-name", value = "gpt-4.1-mini")
public class OpenAiChatModelBuilderTest {

    @Inject
    BeanContext beanContext;

    @Test
    void openAiChatModelBuilder() {
        beanContext.containsBean(OpenAiChatModel.class);
        beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class);
    }

    @Test
    void namedChatModelsAreResolvableByQualifier() {
        assertEquals(1, beanContext.getBeansOfType(OpenAiChatModel.OpenAiChatModelBuilder.class, Qualifiers.byName("default")).size());
        assertTrue(beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class, Qualifiers.byName("default")));
        assertTrue(beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class, Qualifiers.byName("pirate")));
        assertTrue(beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class, Qualifiers.byName("poet")));
        assertEquals(1, beanContext.getBeansOfType(ChatModel.class, Qualifiers.byName("default")).size());
        assertTrue(beanContext.containsBean(ChatModel.class, Qualifiers.byName("default")));
        assertTrue(beanContext.containsBean(ChatModel.class, Qualifiers.byName("pirate")));
        assertTrue(beanContext.containsBean(ChatModel.class, Qualifiers.byName("poet")));
        assertNotNull(beanContext.getBean(ChatModel.class, Qualifiers.byName("default")));
        assertNotNull(beanContext.getBean(ChatModel.class, Qualifiers.byName("pirate")));
        assertNotNull(beanContext.getBean(ChatModel.class, Qualifiers.byName("poet")));
        assertNotNull(beanContext.getBean(ChatModel.class));
    }

}
