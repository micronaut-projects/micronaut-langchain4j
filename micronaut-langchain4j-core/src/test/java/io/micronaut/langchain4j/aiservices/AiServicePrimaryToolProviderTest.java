package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.ChatModel;
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

@Property(name = "spec.name", value = AiServicePrimaryToolProviderTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServicePrimaryToolProviderTest {

    static final String SPEC_NAME = "AiServicePrimaryToolProviderTest";

    @Inject
    PrimaryToolsAssistant assistant;

    @Test
    void prefersThePrimaryToolProviderToAnUnqualifiedOne() {
        assertEquals("tool said: primary", assistant.chat("hello"));
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
        ToolProvider unqualifiedTools() {
            return tools("unqualified");
        }

        @Singleton
        @Primary
        @Named("custom")
        ToolProvider primaryTools() {
            return tools("primary");
        }

        private static ToolProvider tools(String result) {
            ToolSpecification specification = ToolSpecification.builder().name(result).description(result).build();
            return request -> ToolProviderResult.builder().add(specification, (toolRequest, memoryId) -> result).build();
        }
    }
}

@Requires(property = "spec.name", value = AiServicePrimaryToolProviderTest.SPEC_NAME)
@AiService
interface PrimaryToolsAssistant {
    String chat(String userMessage);
}
