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

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import io.micronaut.langchain4j.annotation.ChatLanguageModelProvider;
import io.micronaut.langchain4j.annotation.ImageLanguageModelProvider;

@ChatLanguageModelProvider(
    value = OpenAiChatModel.class,
    optionalInject = {"tokenizer", "proxy", "listeners"})
@ImageLanguageModelProvider(
    value = OpenAiImageModel.class,
    optionalInject = {"tokenizer", "proxy", "listeners"}
)
final class OpenAiModule {
}
