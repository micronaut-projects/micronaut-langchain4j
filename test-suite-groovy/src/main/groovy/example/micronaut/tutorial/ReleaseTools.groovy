package example.micronaut.tutorial

import dev.langchain4j.agent.tool.Tool
import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Requires(property = "spec.name", value = "KnowledgeAssistantTest")
@Singleton // <1>
@CompileStatic
class ReleaseTools {

    @Tool("Returns the latest release of Micronaut LangChain4j") // <2>
    String latestRelease() {
        return "2.4.0"
    }
}
