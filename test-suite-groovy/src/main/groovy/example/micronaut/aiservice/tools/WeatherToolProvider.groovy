package example.micronaut.aiservice.tools

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.service.tool.ToolExecutor
import dev.langchain4j.service.tool.ToolProvider
import dev.langchain4j.service.tool.ToolProviderRequest
import dev.langchain4j.service.tool.ToolProviderResult
import jakarta.inject.Named
import jakarta.inject.Singleton

@Singleton
@Named("weather") // <1>
class WeatherToolProvider implements ToolProvider {

    private final ToolSpecification forecast = ToolSpecification.builder()
        .name("forecast")
        .description("Returns the weather forecast")
        .build()

    @Override
    ToolProviderResult provideTools(ToolProviderRequest request) { // <2>
        ToolProviderResult.builder()
            .add(forecast, { toolRequest, memoryId -> "sunny" } as ToolExecutor)
            .build()
    }
}
