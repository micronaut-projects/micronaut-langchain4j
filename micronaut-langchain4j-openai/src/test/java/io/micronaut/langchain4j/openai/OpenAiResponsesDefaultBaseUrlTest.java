package io.micronaut.langchain4j.openai;

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
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The Responses API models configured with only a model name call the default OpenAI endpoint.
 */
class OpenAiResponsesDefaultBaseUrlTest {

    private static final String SPEC_NAME = "OpenAiResponsesDefaultBaseUrlTest";

    @Test
    void callsTheResponsesEndpointOfTheDefaultBaseUrl() throws Exception {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.open-ai.api-key", "test",
            "langchain4j.open-ai-responses.chat-models.responses.model-name", "test",
            "langchain4j.open-ai-responses.streaming-chat-models.responses.model-name", "test"))) {
            RecordingHttpClientBuilder recording = context.getBean(RecordingHttpClientBuilder.class);

            ChatModel model = context.getBean(ChatModel.class, Qualifiers.byName("responses"));
            assertThrows(RuntimeException.class, () -> model.chat("Hello"));

            StreamingChatModel streamingModel = context.getBean(StreamingChatModel.class, Qualifiers.byName("responses"));
            CompletableFuture<Throwable> error = new CompletableFuture<>();
            streamingModel.chat("Hello", new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    error.complete(null);
                }

                @Override
                public void onError(Throwable throwable) {
                    error.complete(throwable);
                }
            });
            error.get(10, TimeUnit.SECONDS);

            assertEquals(List.of("https://api.openai.com/v1/responses", "https://api.openai.com/v1/responses"), recording.urls);
        }
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class TestFactory {
        @Singleton
        @Primary
        RecordingHttpClientBuilder recordingHttpClientBuilder() {
            return new RecordingHttpClientBuilder();
        }
    }

    /**
     * Records the URLs of the requests instead of sending them.
     */
    static final class RecordingHttpClientBuilder implements HttpClientBuilder, HttpClient {

        final List<String> urls = new CopyOnWriteArrayList<>();

        @Override
        public Duration connectTimeout() {
            return Duration.ZERO;
        }

        @Override
        public HttpClientBuilder connectTimeout(Duration timeout) {
            return this;
        }

        @Override
        public Duration readTimeout() {
            return Duration.ZERO;
        }

        @Override
        public HttpClientBuilder readTimeout(Duration timeout) {
            return this;
        }

        @Override
        public HttpClient build() {
            return this;
        }

        @Override
        public SuccessfulHttpResponse execute(HttpRequest request) {
            urls.add(request.url());
            throw new IllegalStateException("not sent");
        }

        @Override
        public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
            urls.add(request.url());
            listener.onError(new IllegalStateException("not sent"));
        }
    }
}
