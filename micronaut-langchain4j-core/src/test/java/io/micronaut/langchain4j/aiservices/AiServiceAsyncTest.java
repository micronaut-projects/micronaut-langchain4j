package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = AiServiceAsyncTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServiceAsyncTest {

    static final String SPEC_NAME = "AiServiceAsyncTest";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject
    AsyncAssistant assistant;

    @Inject
    AsyncToolAssistant toolAssistant;

    @Inject
    io.micronaut.context.BeanContext beanContext;

    @Test
    void completableFuture() throws Exception {
        assertEquals("async", assistant.future("Hello").get(10, TimeUnit.SECONDS));
    }

    @Test
    void completionStage() throws Exception {
        assertEquals("async", assistant.stage("Hello").toCompletableFuture().get(10, TimeUnit.SECONDS));
    }

    @Test
    void mono() {
        assertEquals("async", assistant.mono("Hello").block(TIMEOUT));
    }

    @Test
    void flux() {
        assertEquals(List.of("micro", "naut"), assistant.flux("Hello").collectList().block(TIMEOUT));
    }

    @Test
    void reactiveStreamsPublisher() {
        assertEquals(List.of("micro", "naut"), Flux.from(assistant.publisher("Hello")).collectList().block(TIMEOUT));
    }

    @Test
    void asynchronousTools() throws Exception {
        assertEquals("tool said: sunny", toolAssistant.chat("Weather?").get(10, TimeUnit.SECONDS));
    }

    @Test
    void selectsTheModelsFromTheReturnTypes() {
        AiServiceFactory.ModelSelection selection = AiServiceFactory.selectModels(beanContext.getBeanDefinition(AsyncAssistant.class));
        assertEquals(AiServiceFactory.ModelSelection.BOTH, selection);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel() {
            ToolCallingChatModel toolCalling = new ToolCallingChatModel();
            return new ChatModel() {
                @Override
                public ChatResponse doChat(ChatRequest request) {
                    if (request.toolSpecifications() != null && !request.toolSpecifications().isEmpty()
                        || request.messages().getLast() instanceof dev.langchain4j.data.message.ToolExecutionResultMessage) {
                        return toolCalling.doChat(request);
                    }
                    return ChatResponse.builder().aiMessage(AiMessage.from("async")).build();
                }

                // like the providers with a non-blocking HTTP client (OpenAI, Anthropic)
                @Override
                public CompletableFuture<ChatResponse> doChatAsync(ChatRequest request) {
                    return CompletableFuture.supplyAsync(() -> doChat(request));
                }
            };
        }

        @Bean
        @Primary
        StreamingChatModel streamingChatModel() {
            return new StreamingChatModel() {
                @Override
                public void doChat(ChatRequest request, StreamingChatResponseHandler handler) {
                    handler.onPartialResponse("micro");
                    handler.onPartialResponse("naut");
                    handler.onCompleteResponse(ChatResponse.builder().aiMessage(AiMessage.from("micronaut")).build());
                }
            };
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class AsyncWeatherTools {
        @Tool("Returns the weather")
        public CompletableFuture<String> weather() {
            return CompletableFuture.supplyAsync(() -> "sunny");
        }
    }
}

@Requires(property = "spec.name", value = AiServiceAsyncTest.SPEC_NAME)
@AiService(tools = {})
interface AsyncAssistant {
    CompletableFuture<String> future(String userMessage);

    CompletionStage<String> stage(String userMessage);

    Mono<String> mono(String userMessage);

    Flux<String> flux(String userMessage);

    Publisher<String> publisher(String userMessage);
}

@Requires(property = "spec.name", value = AiServiceAsyncTest.SPEC_NAME)
@AiService(tools = AiServiceAsyncTest.AsyncWeatherTools.class)
interface AsyncToolAssistant {
    CompletableFuture<String> chat(String userMessage);
}
