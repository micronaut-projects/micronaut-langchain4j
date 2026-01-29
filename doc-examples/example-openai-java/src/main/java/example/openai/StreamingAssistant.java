package example.openai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import io.micronaut.langchain4j.annotation.AiService;
import reactor.core.publisher.Flux;

@AiService // <1>
public interface StreamingAssistant {

    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage); // <2>
}
