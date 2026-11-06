package example.micronaut.mcp;

import dev.langchain4j.agentic.declarative.McpClientAgent;
import dev.langchain4j.agentic.declarative.McpClientSupplier;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.V;
import io.micronaut.context.annotation.Property;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * An agentic workflow calls a tool of an MCP server through an MCP client agent, whose client is a Micronaut bean.
 */
@Property(name = "micronaut.mcp.server.info.name", value = "time-server")
@Property(name = "micronaut.mcp.server.info.version", value = "1.0.0")
@Property(name = "micronaut.mcp.server.transport", value = "HTTP")
@MicronautTest
class McpClientAgentTest {

    @Inject
    ClockWorkflow workflow;

    @Test
    void callsTheToolThroughTheMcpClientBean() {
        assertEquals("noon", workflow.time("UTC"));
    }

    @AgenticService
    public interface ClockWorkflow {
        @SequenceAgent(outputKey = "time", subAgents = ClockAgent.class)
        String time(@V("zone") String zone);
    }

    public interface ClockAgent {
        @McpClientAgent(toolName = "current-time", outputKey = "time")
        String currentTime();

        @McpClientSupplier
        static McpClient mcpClient(@Named("embeddedServer") McpClient mcpClient) {
            return mcpClient;
        }
    }
}
