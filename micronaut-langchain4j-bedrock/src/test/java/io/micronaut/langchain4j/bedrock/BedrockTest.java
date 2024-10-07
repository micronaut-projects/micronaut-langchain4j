package io.micronaut.langchain4j.bedrock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.langchain4j.model.bedrock.BedrockLlamaChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@MicronautTest
@Property(name = "langchain4j.bedrock-llama.api-key", value = "blah")
public class BedrockTest {

    @Inject
    DefaultBedrockLlamaChatModelConfiguration chatLanguageModelConfiguration;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        BedrockLlamaChatModel model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    @MockBean
    AwsCredentialsProvider awsCredentialsProvider() {
        return () -> null;
    }
}
