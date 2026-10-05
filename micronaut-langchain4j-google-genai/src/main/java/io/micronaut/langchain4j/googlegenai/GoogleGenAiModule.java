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
package io.micronaut.langchain4j.googlegenai;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.google.genai.GoogleGenAiChatModel;
import dev.langchain4j.model.google.genai.GoogleGenAiEmbeddingModel;
import dev.langchain4j.model.google.genai.GoogleGenAiImageModel;
import dev.langchain4j.model.google.genai.GoogleGenAiStreamingChatModel;
import dev.langchain4j.model.image.ImageModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;

/**
 * Provides integration with the Google Gen AI SDK, for the Gemini API and Vertex AI. The models share the
 * {@link com.google.genai.Client} bean configured with {@link GoogleGenAiClientConfiguration}.
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = ChatModel.class,
            impl = GoogleGenAiChatModel.class,
            defaultModelName = GoogleGenAiModule.DEFAULT_CHAT_MODEL
        ),
        @Model(
            kind = StreamingChatModel.class,
            impl = GoogleGenAiStreamingChatModel.class,
            defaultModelName = GoogleGenAiModule.DEFAULT_CHAT_MODEL
        ),
        @Model(
            kind = EmbeddingModel.class,
            impl = GoogleGenAiEmbeddingModel.class,
            defaultModelName = GoogleGenAiModule.DEFAULT_EMBEDDING_MODEL
        ),
        @Model(
            kind = ImageModel.class,
            impl = GoogleGenAiImageModel.class,
            configRequired = true
        )
    },
    properties = {
        @Lang4jConfig.Property(name = "client", injected = true, required = true),
        // the configuration binding sets an empty map, which the SDK rejects outside of Vertex AI
        @Lang4jConfig.Property(name = "labels", excluded = true)
    }
)
final class GoogleGenAiModule {
    static final String DEFAULT_CHAT_MODEL = "gemini-2.5-flash";
    static final String DEFAULT_EMBEDDING_MODEL = "gemini-embedding-001";
}
