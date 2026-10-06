/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.langchain4j.voyageai;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel;
import dev.langchain4j.model.voyageai.VoyageAiScoringModel;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;

/**
 * Provides integration with Voyage AI: an embedding model and a scoring (re-ranking) model.
 *
 * @since 2.4.0
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = EmbeddingModel.class,
            impl = VoyageAiEmbeddingModel.class,
            defaultModelName = VoyageAiModule.DEFAULT_EMBEDDING_MODEL
        ),
        @Model(
            kind = ScoringModel.class,
            impl = VoyageAiScoringModel.class,
            defaultModelName = VoyageAiModule.DEFAULT_SCORING_MODEL
        )
    },
    properties = {
        @Lang4jConfig.Property(name = "httpClientBuilder", injected = true),
        @Lang4jConfig.Property(name = "baseUrl", common = true),
        @Lang4jConfig.Property(name = "apiKey", common = true, required = true),
        @Lang4jConfig.Property(name = "timeout", common = true),
        @Lang4jConfig.Property(name = "logRequests", common = true, defaultValue = StringUtils.FALSE),
        @Lang4jConfig.Property(name = "logResponses", common = true, defaultValue = StringUtils.FALSE)
    }
)
final class VoyageAiModule {
    static final String DEFAULT_EMBEDDING_MODEL = "voyage-3.5";
    static final String DEFAULT_SCORING_MODEL = "rerank-2.5";

    private VoyageAiModule() {
    }
}
