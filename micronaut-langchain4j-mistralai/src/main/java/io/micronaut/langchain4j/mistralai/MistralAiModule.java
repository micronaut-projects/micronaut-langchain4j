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
package io.micronaut.langchain4j.mistralai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
@Lang4jConfig(
    models = {
        @Lang4jConfig.Model(
            kind = ChatLanguageModel.class,
            impl = MistralAiChatModel.class)
        ,
        @Lang4jConfig.Model(
            kind = StreamingChatLanguageModel.class,
            impl = MistralAiStreamingChatModel.class
        ),
        @Lang4jConfig.Model(
            kind = EmbeddingModel.class,
            impl = MistralAiEmbeddingModel.class
        )
    },
    properties = {
        @Lang4jConfig.Property(name = "baseUrl", common = true, required = true, defaultValue = "https://api.mistral.ai/v1/"),
        @Lang4jConfig.Property(name = "modelName", common = true, required = true, defaultValue = "mistral-tiny"),
        @Lang4jConfig.Property(name = "apiKey", common = true, required = true),
        @Lang4jConfig.Property(name = "logRequests", common = true, defaultValue = StringUtils.FALSE),
        @Lang4jConfig.Property(name = "logResponses", common = true, defaultValue = StringUtils.FALSE)
    }
)
final class MistralAiModule {
}
