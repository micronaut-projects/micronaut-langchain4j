package example.micronaut.testing;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;

@AiService
public interface SupportAssistant {

    @SystemMessage("You answer questions about Micronaut")
    String answer(String question);
}
