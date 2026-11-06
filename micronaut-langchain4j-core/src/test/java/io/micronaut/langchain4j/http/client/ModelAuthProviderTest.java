package io.micronaut.langchain4j.http.client;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelAuthProviderTest {

    private static final String SPEC_NAME = "ModelAuthProviderTest";

    private final List<String> authorizations = new CopyOnWriteArrayList<>();
    private HttpServer server;
    private ApplicationContext context;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            authorizations.add(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = "ok".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        context = ApplicationContext.run(Map.of("spec.name", SPEC_NAME));
    }

    @AfterEach
    void stop() {
        context.close();
        server.stop(0);
    }

    @Test
    void replacesTheCredentialsOfTheModel() throws Exception {
        dev.langchain4j.http.client.HttpClient client = context.getBean(HttpClientBuilder.class).build();
        client.execute(request("/rotated"));
        client.executeAsync(request("/rotated")).get(10, TimeUnit.SECONDS);
        client.execute(request("/static"));
        assertEquals(List.of("Bearer rotated-token", "Bearer rotated-token", "Bearer api-key"), authorizations);
    }

    private HttpRequest request(String path) {
        return HttpRequest.builder()
            .method(HttpMethod.POST)
            .url("http://localhost:" + server.getAddress().getPort() + path)
            .addHeader("Authorization", "Bearer api-key")
            .addHeader("Content-Type", "application/json")
            .body("{}")
            .build();
    }

    /**
     * Rotates the credentials of the requests whose path starts with {@code /rotated}.
     */
    @Singleton
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class RotatingModelAuthProvider implements ModelAuthProvider {
        @Override
        public @Nullable String authorization(ModelAuthRequest request) {
            return request.uri().getPath().startsWith("/rotated") ? "Bearer rotated-token" : null;
        }
    }
}
