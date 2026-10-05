package example.micronaut.aiservice.tools;

import io.micronaut.langchain4j.annotation.AiService;

@AiService(toolProviders = "weather") // <1>
public interface WeatherAssistant {
    String ask(String question);
}
