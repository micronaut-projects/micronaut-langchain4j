package io.micronaut.langchain4j.bedrock;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;

@Factory
public class BedrockTitanEmbeddingModelFactory {
    @Context
    @Bean(
        typed = EmbeddingModel.class
    )
    @EachBean(NamedBedrockTitanEmbeddingModelConfiguration.class)
    protected EmbeddingModel model(NamedBedrockTitanEmbeddingModelConfiguration configuration) {
        return configuration.builder.build();
    }

    @Primary
    @Context
    @Bean(
        typed = EmbeddingModel.class
    )
    @EachBean(NamedBedrockTitanEmbeddingModelConfiguration.class)
    protected EmbeddingModel primaryModel(DefaultBedrockTitanEmbeddingModelConfiguration configuration) {
        return configuration.builder.build();
    }
}
