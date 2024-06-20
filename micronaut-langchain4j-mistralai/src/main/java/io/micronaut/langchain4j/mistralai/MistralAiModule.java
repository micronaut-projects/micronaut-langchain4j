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
package io.micronaut.langchain4j.mistralai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiChatModel;
import dev.langchain4j.model.mistralai.MistralAiEmbeddingModel;
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel;
import io.micronaut.langchain4j.annotation.ModelProvider;

@ModelProvider(
    kind = ChatLanguageModel.class,
    impl = MistralAiChatModel.class
)
@ModelProvider(
    kind = StreamingChatLanguageModel.class,
    impl = MistralAiStreamingChatModel.class
)
@ModelProvider(
    kind = EmbeddingModel.class,
    impl = MistralAiEmbeddingModel.class
)
final class MistralAiModule {
}
