package example.micronaut.aiservice.evaluation;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService
public interface EvaluatingFriend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    Result<String> chat(String userMessage);
}
