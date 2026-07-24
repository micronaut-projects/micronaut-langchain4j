package example.micronaut.aiservice.evaluation;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;

@Requires(property = "spec.name", value = "AiServiceEvaluationExample")
@AiService
public interface EvaluatingFriend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    Result<String> chat(String userMessage);
}
