package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolExecutionResult;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * The tools of the MCP client beans named by {@code @AiService(mcpClients)} are offered to the service.
 */
@Property(name = "spec.name", value = AiServiceMcpClientsTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServiceMcpClientsTest {

    static final String SPEC_NAME = "AiServiceMcpClientsTest";

    @Inject
    ToolCallingChatModel chatModel;

    @Inject
    TimeAssistant timeAssistant;

    @BeforeEach
    void reset() {
        chatModel.reset();
    }

    @Test
    void callsTheToolsOfTheNamedMcpClient() {
        assertEquals("tool said: noon", timeAssistant.chat("What time is it?"));
        assertEquals(List.of("current-time"), chatModel.lastToolNames());
    }

    @Test
    @SuppressWarnings("removal")
    void theDeprecatedConstructorUsesTheDefaultToolProvider() {
        AiServiceDef<Object> def = new AiServiceDef<>(null, Object.class, "name", null, null);
        assertNull(def.toolProviders());
        assertNull(def.mcpClients());
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService(mcpClients = "clock")
    interface TimeAssistant {
        String chat(String message);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Clients {

        @Singleton
        ToolCallingChatModel chatModel() {
            return new ToolCallingChatModel();
        }

        @Singleton
        @Named("clock")
        McpClient clock() {
            ToolSpecification tool = ToolSpecification.builder().name("current-time").description("The current time").build();
            return (McpClient) Proxy.newProxyInstance(McpClient.class.getClassLoader(), new Class<?>[]{McpClient.class}, (proxy, method, args) -> switch (method.getName()) {
                case "key" -> "clock";
                case "listTools" -> List.of(tool);
                case "executeTool" -> ToolExecutionResult.builder().resultText("noon").build();
                case "close", "checkHealth" -> null;
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                case "toString" -> "clock";
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }
    }
}
