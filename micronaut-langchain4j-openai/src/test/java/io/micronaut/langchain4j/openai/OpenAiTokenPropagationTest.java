package io.micronaut.langchain4j.openai;

import com.sun.net.httpserver.HttpServer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.HttpClient;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.security.token.validator.TokenValidator;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The bearer token of the user calling the application, authenticated by Micronaut Security, is sent to the
 * OpenAI-compatible endpoint in place of the API key.
 */
class OpenAiTokenPropagationTest {

    private static final String SPEC_NAME = "OpenAiTokenPropagationTest";

    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private HttpServer modelServer;
    private EmbeddedServer server;

    @BeforeEach
    void start() throws IOException {
        modelServer = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        modelServer.createContext("/chat/completions", exchange -> {
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] response = """
                {"id":"c","object":"chat.completion","created":0,"model":"test",
                 "choices":[{"index":0,"message":{"role":"assistant","content":"hello"},"finish_reason":"stop"}],
                 "usage":{"prompt_tokens":3,"completion_tokens":1,"total_tokens":4}}
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        modelServer.start();
        String modelUrl = "http://localhost:" + modelServer.getAddress().getPort();
        server = ApplicationContext.run(EmbeddedServer.class, Map.of(
            "spec.name", SPEC_NAME,
            "langchain4j.open-ai.api-key", "application-key",
            "langchain4j.open-ai.base-url", modelUrl + "/",
            "langchain4j.open-ai.chat-model.model-name", "test",
            "langchain4j.token-propagation.enabled", true,
            "langchain4j.token-propagation.uri-regex", modelUrl.replace(".", "\\.") + "/.*"
        ));
    }

    @AfterEach
    void stop() {
        server.close();
        modelServer.stop(0);
    }

    @Test
    void propagatesTheTokenOfTheUser() {
        try (HttpClient client = HttpClient.create(server.getURL())) {
            String answer = client.toBlocking().retrieve(HttpRequest.POST("/chat", "Hi").contentType("text/plain").accept("text/plain").bearerAuth("user-token"));
            assertEquals("hello", answer);
        }
        assertEquals(List.of("Bearer user-token"), authorizations);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Assistant {
        String chat(String message);
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller("/chat")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    static class ChatController {
        private final Assistant assistant;

        ChatController(Assistant assistant) {
            this.assistant = assistant;
        }

        @Post(consumes = "text/plain", produces = "text/plain")
        @ExecuteOn(TaskExecutors.BLOCKING)
        String chat(@Body String message) {
            return assistant.chat(message);
        }
    }

    /**
     * Accepts any bearer token, in place of the validation of a JWT or of an opaque token.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class AcceptingTokenValidator implements TokenValidator<HttpRequest<?>> {
        @Override
        public Publisher<Authentication> validateToken(String token, HttpRequest<?> request) {
            return token.equals("user-token") ? Mono.just(Authentication.build("user")) : Flux.empty();
        }
    }
}
