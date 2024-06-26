/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.huggingface;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.huggingface.HuggingFaceChatModel;
import dev.langchain4j.model.huggingface.HuggingFaceEmbeddingModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

/**
 * A module to integrate Hugging Face.
 */
@Lang4jConfig(
    models = {
        @Lang4jConfig.Model(
            kind = ChatLanguageModel.class,
            impl = HuggingFaceChatModel.class)
        ,
        @Lang4jConfig.Model(
            kind = EmbeddingModel.class,
            impl = HuggingFaceEmbeddingModel.class
        )
    },
    properties = {
        @Lang4jConfig.Property(name = "modelId", common = true, required = true, defaultValue = "tiiuae/falcon-7b-instruct"),
        @Lang4jConfig.Property(name = "accessToken", common = true, required = true),
        @Lang4jConfig.Property(name = "timeout", common = true)
    }
)
final class HuggingFaceModule {
}
