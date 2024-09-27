package example;

import io.micronaut.langchain4j.annotation.AiService;

@AiService(tools = Calculator.class)
public interface MathGenius {
    String ask(String question);
}
