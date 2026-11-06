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
package io.micronaut.langchain4j.dev;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * A chat model of the test classpath, which the development runtime treats as part of the runtime classpath: it
 * calls the first tool it is offered, then answers with what the tool returned and the system message, separated by
 * a bar, so that a test sees which tool and which system message the AI service was built with.
 */
@Singleton
@Bean(typed = ChatModel.class)
@Requires(property = "test.chat-model", value = "scripted")
public class ScriptedChatModel implements ChatModel {

    @Override
    public ChatResponse doChat(ChatRequest request) {
        List<ChatMessage> messages = request.messages();
        List<ToolSpecification> tools = request.toolSpecifications();
        String toolResult = null;
        String system = "";
        for (ChatMessage message : messages) {
            if (message instanceof ToolExecutionResultMessage result) {
                toolResult = result.text();
            } else if (message instanceof SystemMessage systemMessage) {
                system = systemMessage.text();
            }
        }
        if (toolResult == null && tools != null && !tools.isEmpty()) {
            ToolExecutionRequest call = ToolExecutionRequest.builder()
                .id("1")
                .name(tools.get(0).name())
                .arguments("{}")
                .build();
            return ChatResponse.builder().aiMessage(AiMessage.from(call)).build();
        }
        return ChatResponse.builder().aiMessage(AiMessage.from((toolResult == null ? "" : toolResult) + "|" + system)).build();
    }
}
