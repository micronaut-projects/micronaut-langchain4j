package io.micronaut.langchain4j.chatmodels.azure;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = AzureOpenAiChatModelConfiguration.PREFIX, primary = "default")
@Requires(classes = AzureOpenAiChatModel.class)
public class AzureOpenAiChatModelConfiguration {
    public static final String PREFIX = "langchain4j.azure-openai.chat-models";
    @ConfigurationBuilder(prefixes = "", excludes = {"token-credential", "tokenizer"})
    AzureOpenAiChatModel.Builder builder = AzureOpenAiChatModel.builder();
}
