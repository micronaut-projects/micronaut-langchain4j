package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.inject.Singleton;

@Singleton
class DefaultJokeGenerator implements JokeGenerator {
    private final ChatModel model;
    DefaultJokeGenerator(ChatModel model) {
        this.model = model;
    }

    @Override
    public String generateJoke() {
        return model.chat("Tell me a joke about Java?");
    }
}
