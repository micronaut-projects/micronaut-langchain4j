package io.micronaut.langchain4j.chatmodels.tck.tests;

import dev.langchain4j.model.chat.ChatLanguageModel;
import jakarta.inject.Singleton;

@Singleton
class DefaultJokeGenerator implements JokeGenerator {
    private final ChatLanguageModel model;
    DefaultJokeGenerator(ChatLanguageModel model) {
        this.model = model;
    }

    @Override
    public String generateJoke() {
        return model.generate("Tell me a joke about Java?");
    }
}
