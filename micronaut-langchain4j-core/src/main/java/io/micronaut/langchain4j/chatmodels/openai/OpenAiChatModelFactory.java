package io.micronaut.langchain4j.chatmodels.openai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.chatmodels.ollama.OllamaChatModelConfiguration;

@Requires(classes = OpenAiChatModel.class)
@Factory
public class OpenAiChatModelFactory {
    @EachBean(OllamaChatModelConfiguration.class)
    protected OpenAiChatModel.OpenAiChatModelBuilder models(OpenAiChatModelConfiguration config) {
        return config.builder;
    }

    @EachBean(OpenAiChatModel.OpenAiChatModelBuilder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected OpenAiChatModel chatModel(OpenAiChatModel.OpenAiChatModelBuilder builder) {
        return builder.build();
    }
}
