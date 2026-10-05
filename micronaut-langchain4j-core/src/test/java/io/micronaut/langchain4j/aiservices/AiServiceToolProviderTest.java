package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.tool.ToolExecutor;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.service.tool.ToolProviderResult;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = AiServiceToolProviderTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServiceToolProviderTest {

    static final String SPEC_NAME = "AiServiceToolProviderTest";

    @Inject
    ToolCallingChatModel chatModel;

    @Inject
    WeatherAssistant weatherAssistant;

    @Inject
    UnnamedAssistant unnamedAssistant;

    @Inject
    ExplicitAssistant explicitAssistant;

    @Inject
    NoToolsAssistant noToolsAssistant;

    @BeforeEach
    void reset() {
        chatModel.reset();
    }

    @Test
    void usesTheToolProviderNamedAfterTheService() {
        assertEquals("tool said: sunny", weatherAssistant.chat("weather?"));
        assertEquals(List.of("weather"), chatModel.lastToolNames());
    }

    @Test
    void namedToolProvidersAreNotUsedImplicitly() {
        assertEquals("no tools", unnamedAssistant.chat("hello"));
        assertEquals(List.of(), chatModel.lastToolNames());
    }

    @Test
    void usesTheExplicitlyNamedToolProviders() {
        explicitAssistant.chat("hello");
        assertEquals(List.of("time", "weather"), chatModel.lastToolNames());
    }

    @Test
    void anEmptyArrayDisablesToolProviders() {
        assertEquals("no tools", noToolsAssistant.chat("weather?"));
    }

    static ToolProvider toolProvider(String toolName, String result) {
        ToolSpecification specification = ToolSpecification.builder().name(toolName).description(toolName).build();
        ToolExecutor executor = (request, memoryId) -> result;
        return request -> ToolProviderResult.builder().add(specification, executor).build();
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Singleton
        ToolCallingChatModel toolCallingChatModel() {
            return new ToolCallingChatModel();
        }

        @Bean
        @Primary
        ChatModel chatModel(ToolCallingChatModel model) {
            return model;
        }

        @Singleton
        @Named("weather")
        ToolProvider weatherTools() {
            return toolProvider("weather", "sunny");
        }

        @Singleton
        @Named("time")
        ToolProvider timeTools() {
            return toolProvider("time", "noon");
        }
    }
}

@Requires(property = "spec.name", value = AiServiceToolProviderTest.SPEC_NAME)
@AiService(named = "weather")
interface WeatherAssistant {
    String chat(String userMessage);
}

@Requires(property = "spec.name", value = AiServiceToolProviderTest.SPEC_NAME)
@AiService
interface UnnamedAssistant {
    String chat(String userMessage);
}

@Requires(property = "spec.name", value = AiServiceToolProviderTest.SPEC_NAME)
@AiService(toolProviders = {"weather", "time"})
interface ExplicitAssistant {
    String chat(String userMessage);
}

@Requires(property = "spec.name", value = AiServiceToolProviderTest.SPEC_NAME)
@AiService(named = "weather", toolProviders = {})
interface NoToolsAssistant {
    String chat(String userMessage);
}
