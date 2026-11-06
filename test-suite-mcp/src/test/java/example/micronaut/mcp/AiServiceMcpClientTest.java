package example.micronaut.mcp;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.mcp.annotations.Tool;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Calls a tool of an MCP server (the embedded Micronaut MCP server, over Streamable HTTP) from an AI service.
 */
@Property(name = "micronaut.mcp.server.info.name", value = "time-server")
@Property(name = "micronaut.mcp.server.info.version", value = "1.0.0")
@Property(name = "micronaut.mcp.server.transport", value = "HTTP")
@MicronautTest
class AiServiceMcpClientTest {

    @Inject
    McpAssistant mcpAssistant;

    @Inject
    DefaultProviderAssistant defaultProviderAssistant;

    @Test
    void callsTheToolsOfTheNamedMcpClient() {
        assertEquals("tools: current-time; result: noon", mcpAssistant.chat("What time is it?"));
    }

    @Test
    void usesTheMcpToolProviderBeanByDefault() {
        assertEquals("tools: current-time; result: noon", defaultProviderAssistant.chat("What time is it?"));
    }

    @AiService(mcpClients = "embeddedServer")
    interface McpAssistant {
        String chat(String userMessage);
    }

    @AiService
    interface DefaultProviderAssistant {
        String chat(String userMessage);
    }

    @Singleton
    static class TimeTools {
        @Tool(name = "current-time", description = "Provides the current time")
        String currentTime() {
            return "noon";
        }
    }

    @Factory
    static class ChatModelFactory {
        /**
         * Calls the first tool it is offered, then answers with the tool names and the tool result.
         */
        @Bean
        @Primary
        ChatModel chatModel() {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    List<ToolSpecification> tools = request.toolSpecifications();
                    if (request.messages().getLast() instanceof ToolExecutionResultMessage result) {
                        String names = tools.stream().map(ToolSpecification::name).collect(Collectors.joining(","));
                        return ChatResponse.builder().aiMessage(AiMessage.from("tools: " + names + "; result: " + result.text())).build();
                    }
                    ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                        .id("1")
                        .name(tools.getFirst().name())
                        .arguments("{}")
                        .build();
                    return ChatResponse.builder().aiMessage(AiMessage.from(toolRequest)).build();
                }
            };
        }
    }
}
