package io.micronaut.langchain4j.decision;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.router.ChatModelRouter;
import dev.langchain4j.model.chat.router.ChatModelRoutingResult;
import dev.langchain4j.model.decision.DecisionModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoutingChatModelOptionsTest {

    static final String SPEC_NAME = "RoutingChatModelOptionsTest";

    @Test
    void usesTheNamedRouter() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.routing-chat-models.assistant.routes.fast", "small",
            "langchain4j.routing-chat-models.assistant.routes.expert", "large",
            "langchain4j.routing-chat-models.assistant.default-route", "fast",
            "langchain4j.routing-chat-models.assistant.router", "always-expert"))) {
            assertEquals("large", context.getBean(ChatModel.class, Qualifiers.byName("assistant")).chat("Hello"));
        }
    }

    @Test
    void configuresTheDecisionModelRouter() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.routing-chat-models.assistant.routes.fast", "small",
            "langchain4j.routing-chat-models.assistant.routes.expert", "large",
            "langchain4j.routing-chat-models.assistant.descriptions.fast", "Greetings and small talk",
            "langchain4j.routing-chat-models.assistant.descriptions.expert", "Mathematics, reasoning and code",
            "langchain4j.routing-chat-models.assistant.default-route", "fast",
            "langchain4j.routing-chat-models.assistant.decision-model", "keyword",
            "langchain4j.routing-chat-models.assistant.question", "Which model should answer?",
            "langchain4j.routing-chat-models.assistant.min-probability", "0.5",
            "langchain4j.routing-chat-models.assistant.fallback-strategy", "DEFAULT_ROUTE"))) {
            ChatModel assistant = context.getBean(ChatModel.class, Qualifiers.byName("assistant"));
            assertEquals("large", assistant.chat("Solve this mathematics problem: 12 * 7"));
            assertEquals("small", assistant.chat("Greetings! Just some small talk"));
        }
    }

    @Test
    void requiresRoutes() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.routing-chat-models.empty.default-route", "fast"))) {
            BeanInstantiationException error = assertThrows(BeanInstantiationException.class,
                () -> context.getBean(ChatModel.class, Qualifiers.byName("empty")));
            assertTrue(error.getMessage().contains("The routing chat model 'empty' has no routes"), error.getMessage());
        }
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Models {
        @Singleton
        @Named("keyword")
        DecisionModel keyword() {
            return new KeywordDecisionModel();
        }

        @Singleton
        @Named("always-expert")
        ChatModelRouter alwaysExpert() {
            return request -> ChatModelRoutingResult.route("expert");
        }

        @Singleton
        @Named("small")
        ChatModel small() {
            return named("small");
        }

        @Singleton
        @Named("large")
        ChatModel large() {
            return named("large");
        }

        private static ChatModel named(String name) {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    return ChatResponse.builder().aiMessage(AiMessage.from(name)).build();
                }
            };
        }
    }
}
