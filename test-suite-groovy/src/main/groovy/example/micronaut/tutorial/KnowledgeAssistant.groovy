package example.micronaut.tutorial

import dev.langchain4j.service.Result
import dev.langchain4j.service.SystemMessage
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.annotation.AiService

@Requires(property = "spec.name", value = "KnowledgeAssistantTest")
@AiService(value = "knowledge", tools = ReleaseTools.class) // <1>
interface KnowledgeAssistant {

    @SystemMessage("You answer questions about Micronaut from the documents you are given") // <2>
    Result<String> ask(String question) // <3>
}
