package example.micronaut.graal;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Chat model of the tests: it asks for the first tool it is offered, then answers with the result of the tool, or,
 * without tools, with the system message and the user message it received.
 */
@Singleton
public class EchoChatModel implements ChatModel {

    @Override
    public ChatResponse doChat(ChatRequest chatRequest) {
        List<ChatMessage> messages = chatRequest.messages();
        ChatMessage last = messages.getLast();
        if (last instanceof ToolExecutionResultMessage result) {
            return answer("tool:" + result.text());
        }
        if (chatRequest.toolSpecifications() != null && !chatRequest.toolSpecifications().isEmpty()) {
            ToolExecutionRequest request = ToolExecutionRequest.builder()
                .id("1")
                .name(chatRequest.toolSpecifications().getFirst().name())
                .arguments("{\"document\": \"privacy\"}")
                .build();
            return ChatResponse.builder().aiMessage(AiMessage.from(List.of(request))).build();
        }
        String system = messages.stream()
            .filter(SystemMessage.class::isInstance)
            .map(message -> ((SystemMessage) message).text() + "|")
            .findFirst()
            .orElse("");
        return answer(system + ((UserMessage) last).singleText());
    }

    private static ChatResponse answer(String text) {
        return ChatResponse.builder().aiMessage(AiMessage.from(text)).build();
    }
}
