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
package io.micronaut.langchain4j.azure.openai;

import com.azure.ai.openai.models.ChatCompletionsJsonResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsResponseFormat;
import com.azure.ai.openai.models.ChatCompletionsTextResponseFormat;
import dev.langchain4j.model.azure.AzureOpenAiChatModel;
import dev.langchain4j.model.azure.AzureOpenAiEmbeddingModel;
import dev.langchain4j.model.azure.AzureOpenAiImageModel;
import dev.langchain4j.model.azure.AzureOpenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import io.micronaut.core.convert.MutableConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;
import jakarta.inject.Singleton;

@Lang4jConfig(
    models = {
        @Model(
            kind = ChatLanguageModel.class,
            impl = AzureOpenAiChatModel.class)
        ,
        @Model(
            kind = StreamingChatLanguageModel.class,
            impl = AzureOpenAiStreamingChatModel.class
        ),
        @Model(
            kind = ImageModel.class,
            impl = AzureOpenAiImageModel.class
        ),
        @Model(
            kind = EmbeddingModel.class,
            impl = AzureOpenAiEmbeddingModel.class
        )
    },
    properties = {
        @Property(
            name = "tokenCredential",
            injected = true,
            required = true
        ),
        @Property(
            name = "proxyOptions",
            injected = true
        ),
        @Property(
            name = "keyCredential",
            injected = true
        ),
        @Property(
            name = "dataSources",
            injected = true
        ),
        @Property(
            name = "tokenizer",
            injected = true
        ),
        @Property(name = "apiKey", common = true, required = true),
        @Property(name = "endpoint", common = true),
        @Property(name = "serviceVersion", common = true),
        @Property(name = "deploymentName", common = true),
        @Property(name = "timeout", common = true),
        @Property(name = "maxRetries", common = true),
        @Property(name = "logRequestsAndResponses", common = true, required = true, defaultValue = "false")
    }
)

@Singleton
final class AzureOpenAiModule implements TypeConverterRegistrar {
    @Override
    public void register(MutableConversionService conversionService) {
        conversionService.addConverter(
            String.class,
            ChatCompletionsResponseFormat.class,
            (val) -> switch (val) {
                case "json_object" -> new ChatCompletionsJsonResponseFormat();
                default -> new ChatCompletionsTextResponseFormat();
            }
        );
    }
}
