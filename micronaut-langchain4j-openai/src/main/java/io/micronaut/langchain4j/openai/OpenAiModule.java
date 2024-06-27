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
package io.micronaut.langchain4j.openai;

import static io.micronaut.langchain4j.annotation.Lang4jConfig.*;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;

/**
 * Provides integration with OpenAI services.
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = ChatLanguageModel.class,
            impl = OpenAiChatModel.class,
            defaultModelName = OpenAiModule.DEFAULT_CHAT_MODEL
        ),
        @Model(
            kind = StreamingChatLanguageModel.class,
            impl = OpenAiStreamingChatModel.class,
            defaultModelName = OpenAiModule.DEFAULT_CHAT_MODEL
        ),
        @Model(
            kind = ModerationModel.class,
            impl = OpenAiModerationModel.class),
        @Model(
            kind = ImageModel.class,
            impl = OpenAiImageModel.class,
            defaultModelName = OpenAiModule.DEFAULT_IMAGE_MODEL
        ),
        @Model(
            kind = EmbeddingModel.class,
            impl = OpenAiEmbeddingModel.class,
            defaultModelName = OpenAiModule.DEFAULT_EMBEDDING_MODEL
        )
    },
    properties = {
        @Property(
            name = "proxy",
            injected = true
        ),
        @Property(
            name = "listeners",
            injected = true
        ),
        @Property(
            name = "tokenizer",
            injected = true
        ),
        @Property(name = "baseUrl", common = true, required = true, defaultValue = "https://api.openai.com/v1/"),
        @Property(name = "apiKey", common = true, required = true),
        @Property(name = "organizationId", common = true),
        @Property(name = "timeout", common = true),
        @Property(name = "logRequests", common = true, defaultValue = StringUtils.FALSE),
        @Property(name = "logResponses", common = true, defaultValue = StringUtils.FALSE)
    }
)
final class OpenAiModule {
    public static final String DEFAULT_CHAT_MODEL = "gpt-3.5-turbo";
    public static final String DEFAULT_IMAGE_MODEL = "dall-e-3";
    public static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-ada-002";
}
