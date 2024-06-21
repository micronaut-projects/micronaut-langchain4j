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
package io.micronaut.langchain4j.anthropic;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import io.micronaut.core.annotation.Internal;
import io.micronaut.langchain4j.annotation.Lang4jConfig;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Model;
import io.micronaut.langchain4j.annotation.Lang4jConfig.Property;

/**
 * Generates the configuration binding for Anthropic.
 */
@Lang4jConfig(
    models = {
        @Model(
            kind = ChatLanguageModel.class,
            impl = AnthropicChatModel.class)
        ,
        @Model(
            kind = StreamingChatLanguageModel.class,
            impl = AnthropicStreamingChatModel.class
        )
    },
    properties = {
        @Property(name = "baseUrl", common = true, required = true, defaultValue = "https://api.anthropic.com/v1/"),
        @Property(name = "modelName", common = true, required = true, defaultValue = "claude-3-haiku-20240307"),
        @Property(name = "apiKey", common = true, required = true),
        @Property(name = "version", common = true),
        @Property(name = "timeout", common = true),
        @Property(name = "logRequests", common = true),
        @Property(name = "logResponses", common = true)
    }
)
@Internal
final class AnthropicModule {
}
