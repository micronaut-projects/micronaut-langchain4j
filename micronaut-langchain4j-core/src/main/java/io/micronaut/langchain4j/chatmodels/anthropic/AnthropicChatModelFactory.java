package io.micronaut.langchain4j.chatmodels.anthropic;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Requires(classes = AnthropicChatModel.class)
@Factory
public class AnthropicChatModelFactory {
    @EachBean(AnthropicChatModelConfiguration.class)
    protected AnthropicChatModel.AnthropicChatModelBuilder models(AnthropicChatModelConfiguration models) {
        return models.builder;
    }

    @EachBean(AnthropicChatModel.AnthropicChatModelBuilder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected AnthropicChatModel chatModel(AnthropicChatModel.AnthropicChatModelBuilder builder) {
        return builder.build();
    }
}
