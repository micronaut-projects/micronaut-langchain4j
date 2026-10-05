package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.tool.ToolErrorContext;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = AiServiceDefaultToolProviderTest.SPEC_NAME)
@Property(name = "langchain4j.ai-services.default.max-tool-calling-round-trips", value = "3")
@Property(name = "langchain4j.ai-services.default.execute-tools-concurrently", value = "true")
@Property(name = "langchain4j.ai-services.default.tool-executor", value = "blocking")
@MicronautTest(startApplication = false, transactional = false)
class AiServiceDefaultToolProviderTest {

    static final String SPEC_NAME = "AiServiceDefaultToolProviderTest";

    @Inject
    DefaultToolsAssistant assistant;

    @Inject
    @Named(AiServiceConfiguration.DEFAULT)
    AiServiceConfiguration configuration;

    @Test
    void bindsTheDefaultConfiguration() {
        assertEquals(3, configuration.getMaxToolCallingRoundTrips());
        assertTrue(configuration.isExecuteToolsConcurrently());
        assertEquals("blocking", configuration.getToolExecutor());
    }

    @Test
    void usesTheDefaultToolProviderAndErrorHandler() {
        assertEquals("tool said: handled: boom", assistant.chat("hello"));
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel() {
            return new ToolCallingChatModel();
        }

        @Singleton
        ToolProvider failingTools() {
            ToolSpecification specification = ToolSpecification.builder().name("fail").description("fails").build();
            ToolExecutor executor = (request, memoryId) -> {
                throw new IllegalStateException("boom");
            };
            return request -> ToolProviderResult.builder().add(specification, executor).build();
        }

        @Singleton
        ToolExecutionErrorHandler errorHandler() {
            return (Throwable error, ToolErrorContext context) -> ToolErrorHandlerResult.text("handled: " + error.getMessage());
        }
    }
}

@Requires(property = "spec.name", value = AiServiceDefaultToolProviderTest.SPEC_NAME)
@AiService
interface DefaultToolsAssistant {
    String chat(String userMessage);
}
