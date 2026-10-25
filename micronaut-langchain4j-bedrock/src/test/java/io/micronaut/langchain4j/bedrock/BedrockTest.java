package io.micronaut.langchain4j.bedrock;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.micronaut.context.annotation.Property;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

@MicronautTest
@Property(name = "langchain4j.bedrock.api-key", value = "blah")
public class BedrockTest {

    @Inject
    DefaultBedrockChatModelConfiguration chatLanguageModelConfiguration;

    @Test
    void testLanguageModel() {
        assertNotNull(chatLanguageModelConfiguration);
        var model = chatLanguageModelConfiguration.getBuilder().build();
        assertNotNull(model);
    }

    @MockBean
    AwsCredentialsProvider awsCredentialsProvider() {
        return () -> null;
    }
}
