package io.micronaut.langchain4j.chatmodels.vertexai;

import dev.langchain4j.model.vertexai.VertexAiGeminiChatModel;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Requires;

@EachProperty(value = VertexAiGeminiChatModelConfiguration.PREFIX, primary = "default")
@Requires(classes = VertexAiGeminiChatModel.class)
public class VertexAiGeminiChatModelConfiguration {
    public static final String PREFIX = "langchain4j.vertxai-gemini.chat-models";
    @ConfigurationBuilder(prefixes = "")
    VertexAiGeminiChatModel.VertexAiGeminiChatModelBuilder builder = VertexAiGeminiChatModel.builder();
}
