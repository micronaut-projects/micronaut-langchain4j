package example.micronaut.graal;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.Capability;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;

/**
 * Chat model of the tests: it asks for the first tool it is offered, then answers with the result of the tool, or,
 * without tools, with the system message and the user message it received, or with the JSON of a user message
 * starting with {@value #JSON}.
 */
@Singleton
public class EchoChatModel implements ChatModel {

    static final String JSON = "JSON ";

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
        String userText = ((UserMessage) last).singleText();
        if (userText.startsWith(JSON)) {
            // structured output: the JSON given in the first line
            return answer(userText.substring(JSON.length()).lines().findFirst().orElse(""));
        }
        String system = messages.stream()
            .filter(SystemMessage.class::isInstance)
            .map(message -> ((SystemMessage) message).text() + "|")
            .findFirst()
            .orElse("");
        return answer(system + userText);
    }

    @Override
    public Set<Capability> supportedCapabilities() {
        // structured outputs are requested with a JSON schema response format
        return Set.of(Capability.RESPONSE_FORMAT_JSON_SCHEMA);
    }

    private static ChatResponse answer(String text) {
        return ChatResponse.builder().aiMessage(AiMessage.from(text)).build();
    }
}
