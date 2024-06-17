package io.micronaut.langchain4j.chatmodels.anthropic;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = AnthropicChatModelConfiguration.PREFIX, primary = "default")
@Requires(classes = AnthropicChatModel.class)
public class AnthropicChatModelConfiguration {
    public static final String PREFIX = "langchain4j.anthropic.chat-models";
    @ConfigurationBuilder(prefixes = "")
    AnthropicChatModel.AnthropicChatModelBuilder builder = AnthropicChatModel.builder();
}
