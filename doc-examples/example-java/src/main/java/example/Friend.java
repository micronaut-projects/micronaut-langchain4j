package example;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.RegisterAiService;

@RegisterAiService // <1>
public interface Friend {

    @SystemMessage("You are a good friend of mine. Answer using slang.") // <2>
    String chat(String userMessage);
}
