package io.micronaut.langchain4j.chatmodels.bedrock;

import dev.langchain4j.model.bedrock.BedrockLlamaChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Requires(classes = BedrockLlamaChatModel.class)
@Factory
public class BedrockChatModelFactory {
    @EachBean(BedrockChatModelConfiguration.class)
    protected BedrockLlamaChatModel.BedrockLlamaChatModelBuilder<?, ?> models(BedrockChatModelConfiguration models) {
        return models.builder;
    }

    @EachBean(BedrockLlamaChatModel.BedrockLlamaChatModelBuilder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected BedrockLlamaChatModel chatModel(BedrockLlamaChatModel.BedrockLlamaChatModelBuilder<?, ?>  builder) {
        return builder.build();
    }
}
