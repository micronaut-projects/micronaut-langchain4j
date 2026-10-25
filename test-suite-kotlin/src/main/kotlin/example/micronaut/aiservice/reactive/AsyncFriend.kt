package example.micronaut.aiservice.reactive

import dev.langchain4j.service.SystemMessage
import io.micronaut.langchain4j.annotation.AiService
import org.reactivestreams.Publisher
import java.util.concurrent.CompletableFuture

@AiService
interface AsyncFriend {

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    fun chat(userMessage: String): CompletableFuture<String> // <1>

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    fun stream(userMessage: String): Publisher<String> // <2>
}
