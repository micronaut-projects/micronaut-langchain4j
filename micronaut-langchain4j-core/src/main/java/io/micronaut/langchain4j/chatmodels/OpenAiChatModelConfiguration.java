package io.micronaut.langchain4j.chatmodels;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiLanguageModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = OpenAiChatModelConfiguration.PREFIX, primary = "default")
@Requires(classes = OpenAiLanguageModel.class)
public class OpenAiChatModelConfiguration {
    public static final String PREFIX = "langchain4j.openai.chat-models";
    @ConfigurationBuilder(prefixes = "")
    OpenAiChatModel.OpenAiChatModelBuilder builder = OpenAiChatModel.builder();
}
