package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Chat model test double that calls the first tool it is offered once, then answers with the tool result.
 */
final class ToolCallingChatModel implements ChatModel {

    private final List<ChatRequest> requests = new CopyOnWriteArrayList<>();

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        requests.add(chatRequest);
        List<ChatMessage> messages = chatRequest.messages();
        if (messages.getLast() instanceof ToolExecutionResultMessage result) {
            return ChatResponse.builder().aiMessage(AiMessage.from("tool said: " + result.text())).build();
        }
        List<ToolSpecification> tools = chatRequest.toolSpecifications();
        if (tools == null || tools.isEmpty()) {
            return ChatResponse.builder().aiMessage(AiMessage.from("no tools")).build();
        }
        ToolExecutionRequest request = ToolExecutionRequest.builder()
            .id("1")
            .name(tools.getFirst().name())
            .arguments("{}")
            .build();
        return ChatResponse.builder().aiMessage(AiMessage.from(request)).build();
    }

    List<String> lastToolNames() {
        List<ToolSpecification> tools = requests.getFirst().toolSpecifications();
        return tools == null ? List.of() : tools.stream().map(ToolSpecification::name).sorted().toList();
    }

    void reset() {
        requests.clear();
    }
}
