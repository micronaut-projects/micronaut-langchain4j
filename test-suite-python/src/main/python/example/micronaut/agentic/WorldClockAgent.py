from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.agentic.declarative import McpClientAgent, McpClientSupplier, SequenceAgent
from dev.langchain4j.mcp.client import McpClient
from dev.langchain4j.service import V
from jakarta.inject import Named
from micronaut.context.annotation import Requires
from micronaut.langchain4j.agentic.annotation import AgenticService


# In Python a static function compiles to a static interface method of an introduction type, so the sub-agent
# is an agentic service too
@Requires(beans=[McpClient])
@AgenticService
class CurrentTimeAgent(ABC):
    @McpClientAgent(toolName="current-time", outputKey="time")  # <1>
    @abstractmethod
    def current_time(self, zone: Annotated[str, V("zone")]) -> str:
        ...

    @staticmethod
    @McpClientSupplier
    def mcp_client(mcp_client: Annotated[McpClient, Named("time-server")]) -> McpClient:  # <2>
        return mcp_client


# A workflow whose sub-agent calls a tool of an MCP server through a Micronaut MCP client bean.
@Requires(beans=[McpClient])
@AgenticService
class WorldClockAgent(ABC):

    @SequenceAgent(outputKey="time", subAgents=[CurrentTimeAgent])
    @abstractmethod
    def time(self, zone: Annotated[str, V("zone")]) -> str:
        ...
