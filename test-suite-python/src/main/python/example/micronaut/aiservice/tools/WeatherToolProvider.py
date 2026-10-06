from dev.langchain4j.agent.tool import ToolSpecification
from dev.langchain4j.service.tool import ToolExecutor, ToolProvider, ToolProviderRequest, ToolProviderResult
from jakarta.inject import Named, Singleton


class ForecastExecutor(ToolExecutor):
    def execute(self, tool_request, memory_id) -> str:
        return "sunny"


@Singleton
@Named("weather")  # <1>
class WeatherToolProvider(ToolProvider):

    def provideTools(self, request: ToolProviderRequest) -> ToolProviderResult:  # <2>
        forecast = ToolSpecification.builder() \
            .name("forecast") \
            .description("Returns the weather forecast") \
            .build()
        return ToolProviderResult.builder() \
            .add(forecast, ForecastExecutor()) \
            .build()
