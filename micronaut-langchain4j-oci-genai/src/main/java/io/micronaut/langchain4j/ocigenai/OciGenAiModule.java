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
package io.micronaut.langchain4j.ocigenai;

/**
 * A module to integrate Oracle Cloud GenAI.
 */
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiChatModel;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiCohereChatModel;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiCohereStreamingChatModel;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import io.micronaut.langchain4j.annotation.Lang4jConfig;

@Lang4jConfig(
    models = {
        @Lang4jConfig.Model(
            kind = ChatModel.class,
            impl = OciGenAiChatModel.class,
            configRequired = true),
        @Lang4jConfig.Model(
            kind = StreamingChatModel.class,
            impl = OciGenAiStreamingChatModel.class,
            configRequired = true),
        @Lang4jConfig.Model(
            kind = ChatModel.class,
            impl = OciGenAiCohereChatModel.class,
            configRequired = true),
        @Lang4jConfig.Model(
            kind = StreamingChatModel.class,
            impl = OciGenAiCohereStreamingChatModel.class,
            configRequired = true)
    },
    properties = {
        @Lang4jConfig.Property(
            name = "genAiClient",
            injected = true,
            required = false
        ),
        @Lang4jConfig.Property(
            name = "authProvider",
            injected = true,
            required = false
        ),
        @Lang4jConfig.Property(
            name = "listeners",
            injected = true,
            required = false
        ),
        @Lang4jConfig.Property(name = "modelName", common = false, required = true),
        @Lang4jConfig.Property(name = "compartmentId", common = true, required = true),
        @Lang4jConfig.Property(name = "region", common = true, required = false),
    }
)
final class OciGenAiModule {
}
