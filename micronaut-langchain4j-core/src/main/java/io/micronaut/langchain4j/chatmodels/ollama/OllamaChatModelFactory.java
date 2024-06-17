package io.micronaut.langchain4j.chatmodels.ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaLanguageModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Requires(classes = OllamaLanguageModel.class)
@Factory
public class OllamaChatModelFactory {
    @EachBean(OllamaChatModelConfiguration.class)
    protected OllamaChatModel.OllamaChatModelBuilder models(OllamaChatModelConfiguration models) {
        return models.builder;
    }

    @EachBean(OllamaChatModel.OllamaChatModelBuilder.class)
    @Bean(typed = ChatLanguageModel.class)
    protected OllamaChatModel chatModel(OllamaChatModel.OllamaChatModelBuilder builder) {
        return builder.build();
    }
}
