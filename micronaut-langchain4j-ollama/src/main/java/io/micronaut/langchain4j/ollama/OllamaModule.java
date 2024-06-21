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
package io.micronaut.langchain4j.ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import io.micronaut.core.annotation.Internal;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;

/**
 * Generates the configuration binding for Ollama.
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = ChatLanguageModel.class,
            impl = OllamaChatModel.class)
        ,
        @Model(
            kind = StreamingChatLanguageModel.class,
            impl = OllamaStreamingChatModel.class
        ),
        @Model(
            kind = EmbeddingModel.class,
            impl = OllamaEmbeddingModel.class
        )
    },
    properties = {
        @Property(name = "baseUrl", common = true, required = true),
        @Property(name = "modelName", common = true, required = true, defaultValue = "llama3"),
        @Property(name = "timeout", common = true),
        @Property(name = "logRequests", common = true),
        @Property(name = "logResponses", common = true)
    }
)
@Internal
final class OllamaModule {
}
