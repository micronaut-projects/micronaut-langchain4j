package example.micronaut.agentic

import dev.langchain4j.agentic.declarative.McpClientAgent
import dev.langchain4j.agentic.declarative.McpClientSupplier
import dev.langchain4j.agentic.declarative.SequenceAgent
import dev.langchain4j.mcp.client.McpClient
import dev.langchain4j.service.V
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.agentic.annotation.AgenticService
import jakarta.inject.Named

/**
 * A workflow whose sub-agent calls a tool of an MCP server through a Micronaut MCP client bean.
 */
@Requires(beans = [McpClient::class])
@AgenticService
interface WorldClockAgent {

    @SequenceAgent(outputKey = "time", subAgents = [CurrentTimeAgent::class])
    fun time(@V("zone") zone: String): String

    interface CurrentTimeAgent {
        @McpClientAgent(toolName = "current-time", outputKey = "time") // <1>
        fun currentTime(@V("zone") zone: String): String

        companion object {
            @McpClientSupplier
            @JvmStatic
            fun mcpClient(@Named("time-server") mcpClient: McpClient): McpClient = mcpClient // <2>
        }
    }
}
