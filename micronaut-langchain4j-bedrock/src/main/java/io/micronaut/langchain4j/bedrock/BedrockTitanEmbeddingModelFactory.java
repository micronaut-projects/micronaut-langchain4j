/*
 * Copyright 2017-2025 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.bedrock;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;

/**
 * Factory for bedrock titan embedding model.
 */
@Factory
public class BedrockTitanEmbeddingModelFactory {
    /**
     * The named embedding models.
     * @param configuration configuration
     * @return The embedding model.
     */
    @Context
    @Bean(
        typed = EmbeddingModel.class
    )
    @EachBean(NamedBedrockTitanEmbeddingModelConfiguration.class)
    protected EmbeddingModel model(NamedBedrockTitanEmbeddingModelConfiguration configuration) {
        return configuration.builder.build();
    }

    /**
     * The primary embedding model.
     * @param configuration The configuration.
     * @return The embedding model
     */
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
