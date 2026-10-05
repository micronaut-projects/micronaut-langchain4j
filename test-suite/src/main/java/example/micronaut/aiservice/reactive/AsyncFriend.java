package example.micronaut.aiservice.reactive;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.langchain4j.annotation.AiService;
import org.reactivestreams.Publisher;

import java.util.concurrent.CompletableFuture;

@AiService
public interface AsyncFriend {

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    CompletableFuture<String> chat(String userMessage); // <1>

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    Publisher<String> stream(String userMessage); // <2>
}
