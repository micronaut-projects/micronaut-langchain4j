package io.micronaut.langchain4j.azure.openai;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.core.credential.TokenCredential;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.image.ImageModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@MicronautTest
@Property(name = "langchain4j.azure-open-ai.api-key", value = "somekey")
@Property(name = "langchain4j.azure-open-ai.endpoint", value = "blah")
@Property(name = "langchain4j.azure-open-ai.chat-models.default.endpoint", value = "blah")
@Property(name = "langchain4j.azure-open-ai.image-models.default.endpoint", value = "blah")
@Property(name = "langchain4j.azure-open-ai.chat-models.default.response-format", value = "json_object")
public class AzureTest {

    @Inject
    @Named("default")
    NamedAzureOpenAiChatModelConfiguration chatLanguageModelConfiguration;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        AzureOpenAiChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    @Test
    void testImageModel(ImageModel imageModel) {
        assertNotNull(imageModel);
    }

    @MockBean
    TokenCredential tokenCredential() {
        return tokenRequestContext -> Mono.empty();
    }

}
