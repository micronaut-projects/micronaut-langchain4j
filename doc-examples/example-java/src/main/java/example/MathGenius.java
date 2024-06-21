package example;

import io.micronaut.langchain4j.annotation.RegisterAiService;

@RegisterAiService(tools = Calculator.class)
public interface MathGenius {
    String ask(String question);
}
