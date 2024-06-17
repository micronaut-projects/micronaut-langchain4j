package io.micronaut.langchain4j.chatmodels.vertexai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Requires(classes = VertexAiGeminiChatModel.class)
@Factory
public class VertexAiGeminiChatModelFactory {
    @EachBean(VertexAiGeminiChatModelConfiguration.class)
    protected VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder models(VertexAiGeminiChatModelConfiguration config) {
        return config.builder;
    }

    @EachBean(VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected VertexAiGeminiChatModel chatModel(VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder builder) {
        return builder.build();
    }
}
