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
package io.micronaut.langchain4j.googleaigemini;

/**
 * A module to integrate Google AI Gemini.
 */
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

@Lang4jConfig(
    models = {
        @Lang4jConfig.Model(
            kind = ChatLanguageModel.class,
            impl = GoogleAiGeminiChatModel.class)
    },
    properties = {
        @Lang4jConfig.Property(name = "modelName", common = true, required = true, defaultValue = "gemini-1.5-flash"),
        @Lang4jConfig.Property(name = "apiKey", common = true, required = true)
    }
)
final class GoogleaiGeminiModule {
}
