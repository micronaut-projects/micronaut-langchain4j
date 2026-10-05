package io.micronaut.langchain4j.controlpanel;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = LangChain4jControlPanelTest.SPEC_NAME)
@MicronautTest(environments = "dev")
class LangChain4jControlPanelTest {

    static final String SPEC_NAME = "LangChain4jControlPanelTest";

    @Inject
    LangChain4jControlPanel controlPanel;

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    void listsTheModelsServicesAndTools() {
        LangChain4jControlPanel.Body body = controlPanel.getBody();
        assertEquals(List.of("default", "pirate"), body.chatModels().stream().map(LangChain4jControlPanel.Model::name).toList());
        assertTrue(body.tools().stream().anyMatch(tool -> tool.name().equals("currentWeather")), body.tools()::toString);
        assertEquals("2", controlPanel.getBadge());
        assertTrue(body.aiServices().contains(PanelAssistant.class.getName()), body.aiServices()::toString);
    }

    @Test
    void rendersTheDetailPage() {
        String html = client.toBlocking().retrieve("/control-panel/langchain4j");
        assertTrue(html.contains("langchain4jChatForm"), html);
        assertTrue(html.contains("currentWeather"), html);
    }

    @Test
    void chatsWithAChatModel() {
        Map<?, ?> reply = client.toBlocking().retrieve(
            HttpRequest.POST("/control-panel/langchain4j-control-panel-controller/chat", Map.of("model", "pirate", "text", "Hello")),
            Map.class);
        assertEquals("Arr, Hello", reply.get("answer"));
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Models {
        @Bean
        @Named("default")
        ChatModel defaultModel() {
            return echo("");
        }

        @Bean
        @Named("pirate")
        ChatModel pirateModel() {
            return echo("Arr, ");
        }

        private static ChatModel echo(String prefix) {
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    String text = ((UserMessage) request.messages().getLast()).singleText();
                    return ChatResponse.builder().aiMessage(AiMessage.from(prefix + text)).build();
                }
            };
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class WeatherTools {
        @Tool("Returns the current weather")
        public String currentWeather() {
            return "sunny";
        }
    }
}

@Requires(property = "spec.name", value = LangChain4jControlPanelTest.SPEC_NAME)
@AiService(named = "pirate")
interface PanelAssistant {
    String chat(String userMessage);
}
