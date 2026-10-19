package example.micronaut.graal;

import io.micronaut.langchain4j.annotation.AiService;

@AiService(named = "ollama", tools = FrameworkTools.class)
public interface OllamaToolAssistant {
    String chat(String userMessage);
}
