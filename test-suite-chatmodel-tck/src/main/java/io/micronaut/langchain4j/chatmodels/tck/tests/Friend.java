package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.service.SystemMessage;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;

@Requires(property = "spec.name", value = "AiServiceTest")
@AiService
public interface Friend {
    @SystemMessage("You are a good friend of mine. Answer using slang.")
    String chat(String userMessage);
}
