package example.micronaut.aiservice.tools;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderRequest;
import dev.langchain4j.service.tool.ToolProviderResult;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("weather") // <1>
public class WeatherToolProvider implements ToolProvider {

    private final ToolSpecification forecast = ToolSpecification.builder()
        .name("forecast")
        .description("Returns the weather forecast")
        .build();

    @Override
    public ToolProviderResult provideTools(ToolProviderRequest request) { // <2>
        return ToolProviderResult.builder()
            .add(forecast, (toolRequest, memoryId) -> "sunny")
            .build();
    }
}
