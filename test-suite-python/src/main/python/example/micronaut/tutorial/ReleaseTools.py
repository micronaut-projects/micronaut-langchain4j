from dev.langchain4j.agent.tool import Tool
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="KnowledgeAssistantTest")
@Singleton  # <1>
class ReleaseTools:

    @Tool("Returns the latest release of Micronaut LangChain4j")  # <2>
    def latestRelease(self) -> str:
        return "2.4.0"
