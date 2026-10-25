package example.micronaut.aiservice.rag;

import dev.langchain4j.service.Result;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;

@Requires(property = "spec.name", value = "RagTest")
@AiService // <1>
public interface Expert {
    Result<String> ask(String question); // <2>
}
