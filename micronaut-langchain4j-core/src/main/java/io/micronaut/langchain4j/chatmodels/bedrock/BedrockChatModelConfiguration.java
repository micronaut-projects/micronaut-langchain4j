package io.micronaut.langchain4j.chatmodels.bedrock;

import dev.langchain4j.model.bedrock.BedrockLlamaChatModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = BedrockChatModelConfiguration.PREFIX, primary = "default")
@Requires(classes = BedrockLlamaChatModel.class)
public class BedrockChatModelConfiguration {
    public static final String PREFIX = "langchain4j.bedrock.chat-models";
    @ConfigurationBuilder(prefixes = "")
    BedrockLlamaChatModel.BedrockLlamaChatModelBuilder<?, ?> builder = BedrockLlamaChatModel.builder();
}
