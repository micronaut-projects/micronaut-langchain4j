package io.micronaut.langchain4j.openai;

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
@Property(name = "langchain4j.open-ai.api-key", value = "blah")
@Property(name = "langchain4j.open-ai.organization-id", value = "blah")
public class OpenAiChatModelBuilderTest {

    @Inject
    BeanContext beanContext;

    @Inject
    ChatModel chatModel;

    @Inject
    StreamingChatModel streamingChatModel;

    @Test
    void openAiChatModelBuilder() {
        assertTrue(beanContext.containsBean(ChatModel.class));
        assertTrue(beanContext.containsBean(OpenAiChatModel.OpenAiChatModelBuilder.class));
    }

    @Test
    void openAiChatModelUsesInjectedHttpClientBuilder() {
        assertEquals("micronaut", chatModel.chat("Which client is active?"));
    }

    @Test
    void openAiStreamingChatModelUsesInjectedHttpClientBuilder() throws InterruptedException {
        StringBuilder partials = new StringBuilder();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch complete = new CountDownLatch(1);

        streamingChatModel.chat("Which client is active?", new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponsePart) {
                partials.append(partialResponsePart);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                complete.countDown();
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
                complete.countDown();
            }
        });

        assertTrue(complete.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals("micronaut", partials.toString());
    }

    @Factory
    static class StubHttpClientBuilderFactory {
        @Bean
        @Primary
        HttpClientBuilder httpClientBuilder() {
            return new StubHttpClientBuilder();
        }
    }

    static final class StubHttpClientBuilder implements HttpClientBuilder {
        private Duration connectTimeout;
        private Duration readTimeout;

        @Override
        public Duration connectTimeout() {
            return connectTimeout;
        }

        @Override
        public HttpClientBuilder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        @Override
        public Duration readTimeout() {
            return readTimeout;
        }

        @Override
        public HttpClientBuilder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        @Override
        public HttpClient build() {
            return new StubHttpClient();
        }
    }

    static final class StubHttpClient implements HttpClient {
        @Override
        public SuccessfulHttpResponse execute(HttpRequest request) throws HttpException, RuntimeException {
            assertEquals("https://api.openai.com/v1/chat/completions", request.url());
            return SuccessfulHttpResponse.builder()
                .statusCode(200)
                .headers(Map.of())
                .body("""
                    {
                      "id": "chatcmpl-test",
                      "object": "chat.completion",
                      "created": 0,
                      "model": "gpt-3.5-turbo",
                      "choices": [
                        {
                          "index": 0,
                          "message": {
                            "role": "assistant",
                            "content": "micronaut"
                          },
                          "finish_reason": "stop"
                        }
                      ],
                      "usage": {
                        "prompt_tokens": 1,
                        "completion_tokens": 1,
                        "total_tokens": 2
                      }
                    }
                    """)
                .build();
        }

        @Override
        public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
            assertEquals("https://api.openai.com/v1/chat/completions", request.url());
            listener.onOpen(SuccessfulHttpResponse.builder()
                .statusCode(200)
                .headers(Map.of())
                .build());
            parser.parse(new ByteArrayInputStream("""
                data: {"id":"chatcmpl-test","object":"chat.completion.chunk","created":0,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"role":"assistant","content":"micro"},"finish_reason":null}]}

                data: {"id":"chatcmpl-test","object":"chat.completion.chunk","created":0,"model":"gpt-3.5-turbo","choices":[{"index":0,"delta":{"content":"naut"},"finish_reason":"stop"}],"usage":{"prompt_tokens":1,"completion_tokens":1,"total_tokens":2}}

                data: [DONE]

                """.getBytes(StandardCharsets.UTF_8)), listener);
            listener.onClose();
        }
    }

}
