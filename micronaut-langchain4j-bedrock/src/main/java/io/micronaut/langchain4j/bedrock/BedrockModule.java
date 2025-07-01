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
package io.micronaut.langchain4j.bedrock;

import dev.langchain4j.model.bedrock.BedrockChatModel;
import dev.langchain4j.model.bedrock.BedrockCohereEmbeddingModel;
import dev.langchain4j.model.bedrock.BedrockStreamingChatModel;
import dev.langchain4j.model.bedrock.BedrockTitanEmbeddingModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;

@Lang4jConfig(
    models = {
        @Model(
            kind = ChatModel.class,
            impl = BedrockChatModel.class),
        @Model(
            kind = StreamingChatModel.class,
            impl = BedrockStreamingChatModel.class),
        @Model(
            kind = EmbeddingModel.class,
            impl = BedrockTitanEmbeddingModel.class,
            exposed = EmbeddingModel.class),
        @Model(
            kind = EmbeddingModel.class,
            impl = BedrockCohereEmbeddingModel.class)
    },
    properties = {
        @Property(
            name = "credentialsProvider",
            injected = true,
            required = true
        ),
        @Property(name = "model", common = true, required = true, defaultValue = "claude-3-haiku-20240307"),
        @Property(name = "region", common = true)
    }
)
final class BedrockModule {
}
