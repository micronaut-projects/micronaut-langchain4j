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
package io.micronaut.langchain4j.vertexai;

import static io.micronaut.langchain4j.annotation.Lang4jConfig.*;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.vertexai.VertexAiChatModel;
import dev.langchain4j.model.vertexai.VertexAiEmbeddingModel;
import dev.langchain4j.model.vertexai.VertexAiImageModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

@Lang4jConfig(
    models = {
        @Model(
            kind = ChatLanguageModel.class,
            impl = VertexAiChatModel.class)
        ,
        @Model(
            kind = ImageModel.class,
            impl = VertexAiImageModel.class
        ),
        @Model(
            kind = EmbeddingModel.class,
            impl = VertexAiEmbeddingModel.class
        )
    },
    properties = {
        @Property(name = "endpoint", common = true, required = true),
        @Property(name = "modelName", common = true, required = true, defaultValue = "chat-bison"),
        @Property(name = "project", common = true, required = true),
        @Property(name = "location", common = true, required = true),
        @Property(name = "publisher", common = true, required = true, defaultValue = "google"),
        @Property(name = "maxRetries", common = true, defaultValue = "5")
    }
)
final class VertexAiModule {
}
