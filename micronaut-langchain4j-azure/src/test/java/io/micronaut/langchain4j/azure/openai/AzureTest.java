package io.micronaut.langchain4j.azure.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.core.credential.TokenCredential;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@MicronautTest
//@Property(name = "langchain4j.azure-open-ai.chat-models.default.model-name", value = "orca-mini")
@Property(name = "langchain4j.azure-open-ai.chat-models.default.endpoint", value = "blah")
@Property(name = "langchain4j.azure-open-ai.chat-models.default.response-format", value = "json_object")
public class AzureTest {

    @Inject
    AzureOpenAiChatModelConfiguration chatLanguageModelConfiguration;

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        AzureOpenAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    @MockBean
    TokenCredential tokenCredential() {
        return tokenRequestContext -> Mono.empty();
    }

}
