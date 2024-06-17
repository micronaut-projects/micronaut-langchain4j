package io.micronaut.langchain4j.chatmodels.azure;

import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Requires(classes = AzureOpenAiChatModel.class)
@Factory
public class AzureOpenAiChatModelFactory {
    @EachBean(AzureOpenAiChatModelConfiguration.class)
    protected AzureOpenAiChatModel.Builder models(AzureOpenAiChatModelConfiguration models) {
        return models.builder;
    }

    @EachBean(AzureOpenAiChatModel.Builder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected AzureOpenAiChatModel chatModel(AzureOpenAiChatModel.Builder builder) {
        return builder.build();
    }
}
