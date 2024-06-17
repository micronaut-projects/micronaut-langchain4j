package io.micronaut.langchain4j.chatmodels;

import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaLanguageModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = OllamaChatModels.PREFIX, primary = "default")
@Requires(classes = OllamaLanguageModel.class)
public class OllamaChatModels {
    public static final String PREFIX = "langchain4j.ollama.chat-models";
    @ConfigurationBuilder(prefixes = "")
    OllamaChatModel.OllamaChatModelBuilder builder = OllamaChatModel.builder();
}
