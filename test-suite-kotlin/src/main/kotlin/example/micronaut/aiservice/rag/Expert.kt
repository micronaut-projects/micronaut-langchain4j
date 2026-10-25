package example.micronaut.aiservice.rag

import dev.langchain4j.service.Result
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.annotation.AiService

@Requires(property = "spec.name", value = "RagTest")
@AiService // <1>
interface Expert {
    fun ask(question: String): Result<String> // <2>
}
