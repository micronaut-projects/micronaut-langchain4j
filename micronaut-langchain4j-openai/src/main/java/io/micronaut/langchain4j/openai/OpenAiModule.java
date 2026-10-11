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

import dev.langchain4j.model.audio.AudioTranscriptionModel;
import dev.langchain4j.model.audio.TextToSpeechModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.model.openai.OpenAiAudioTranscriptionModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiModerationModel;
import dev.langchain4j.model.openai.OpenAiResponsesChatModel;
import dev.langchain4j.model.openai.OpenAiResponsesStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiTextToSpeechModel;
import io.micronaut.core.util.StringUtils;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;

/**
 * Provides integration with OpenAI services.
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = ChatModel.class,
            impl = OpenAiChatModel.class,
            defaultModelName = OpenAiModule.DEFAULT_CHAT_MODEL
        ),
        @Model(
            kind = StreamingChatModel.class,
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
        ),
        @Model(
            kind = AudioTranscriptionModel.class,
            impl = OpenAiAudioTranscriptionModel.class,
            defaultModelName = OpenAiModule.DEFAULT_TRANSCRIPTION_MODEL,
            configRequired = true
        ),
        @Model(
            kind = TextToSpeechModel.class,
            impl = OpenAiTextToSpeechModel.class,
            defaultModelName = OpenAiModule.DEFAULT_TEXT_TO_SPEECH_MODEL,
            configRequired = true
        ),
        @Model(
            kind = ChatModel.class,
            impl = OpenAiResponsesChatModel.class,
            defaultModelName = OpenAiModule.DEFAULT_CHAT_MODEL,
            configRequired = true
        ),
        @Model(
            kind = StreamingChatModel.class,
            impl = OpenAiResponsesStreamingChatModel.class,
            defaultModelName = OpenAiModule.DEFAULT_CHAT_MODEL,
            configRequired = true
        )
    },
    properties = {
        @Lang4jConfig.Property(
            name = "proxy",
            injected = true
        ),
        @Lang4jConfig.Property(
            name = "listeners",
            injected = true
        ),
        @Lang4jConfig.Property(
            name = "tokenizer",
            injected = true
        ),
        @Lang4jConfig.Property(
            name = "httpClientBuilder",
            injected = true
        ),
        @Lang4jConfig.Property(
            name = "httpClientProvider",
            injected = true
        ),
        @Lang4jConfig.Property(name = "baseUrl", common = true, required = true, defaultValue = "https://api.openai.com/v1/"),
        @Lang4jConfig.Property(name = "apiKey", common = true, required = true),
        @Lang4jConfig.Property(name = "organizationId", common = true),
        @Lang4jConfig.Property(name = "timeout", common = true),
        @Lang4jConfig.Property(name = "logRequests", common = true, defaultValue = StringUtils.FALSE),
        @Lang4jConfig.Property(name = "logResponses", common = true, defaultValue = StringUtils.FALSE)
    }
)
final class OpenAiModule {
    public static final String DEFAULT_CHAT_MODEL = "gpt-4.1-mini";
    public static final String DEFAULT_IMAGE_MODEL = "gpt-image-1";
    public static final String DEFAULT_EMBEDDING_MODEL = "text-embedding-3-small";
    public static final String DEFAULT_TRANSCRIPTION_MODEL = "gpt-4o-mini-transcribe";
    public static final String DEFAULT_TEXT_TO_SPEECH_MODEL = "gpt-4o-mini-tts";
}
