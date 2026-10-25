package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import reactor.core.publisher.Flux;

@Requires(property = "spec.name", value = "AiServiceFluxTest")
@AiService
public interface FluxFriend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    Flux<String> chat(String userMessage);
}
