package io.micronaut.langchain4j.decision;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.router.RoutingChatModel;
import dev.langchain4j.model.decision.DecisionModel;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Property(name = "spec.name", value = RoutingChatModelTest.SPEC_NAME)
@Property(name = "langchain4j.routing-chat-models.assistant.routes.fast", value = "small")
@Property(name = "langchain4j.routing-chat-models.assistant.routes.expert", value = "large")
@Property(name = "langchain4j.routing-chat-models.assistant.descriptions.fast", value = "Greetings and small talk")
@Property(name = "langchain4j.routing-chat-models.assistant.descriptions.expert", value = "Mathematics, reasoning and code")
@Property(name = "langchain4j.routing-chat-models.assistant.default-route", value = "fast")
@MicronautTest(startApplication = false)
class RoutingChatModelTest {

    static final String SPEC_NAME = "RoutingChatModelTest";

    @Inject
    @Named("assistant")
    ChatModel assistant;

    @Test
    void routesEachRequest() {
        assertInstanceOf(RoutingChatModel.class, assistant);
        assertEquals("small", assistant.chat("Hello, how is the weather today?"));
        assertEquals("large", assistant.chat("Solve this mathematics problem: 12 * 7"));
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Models {
        @Singleton
        DecisionModel decisionModel() {
            return new KeywordDecisionModel();
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
