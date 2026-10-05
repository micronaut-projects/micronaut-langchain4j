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
class WeatherToolProvider : ToolProvider {

    private val forecast = ToolSpecification.builder()
        .name("forecast")
        .description("Returns the weather forecast")
        .build()

    override fun provideTools(request: ToolProviderRequest): ToolProviderResult = // <2>
        ToolProviderResult.builder()
            .add(forecast, ToolExecutor { _, _ -> "sunny" })
            .build()
}
