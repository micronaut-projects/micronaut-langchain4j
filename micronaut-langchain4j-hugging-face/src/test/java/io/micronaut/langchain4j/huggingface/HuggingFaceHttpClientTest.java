package io.micronaut.langchain4j.huggingface;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The Hugging Face chat model on the Micronaut HTTP client, against a fake inference endpoint.
 */
class HuggingFaceHttpClientTest {

    @Test
    void chatsWithTheMicronautHttpClient() throws Exception {
        List<String> requests = new CopyOnWriteArrayList<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            requests.add(exchange.getRequestURI().getPath());
            byte[] response = "[{\"generated_text\":\"micronaut\"}]".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.hugging-face.access-token", "test",
            "langchain4j.hugging-face.chat-model.base-url", "http://localhost:" + server.getAddress().getPort() + "/"))) {
            assertEquals("micronaut", context.getBean(ChatModel.class).chat("Which framework?"));
            // LangChain4j's JDK HTTP client is excluded: the request can only have been sent by the Micronaut HTTP client
            assertThrows(ClassNotFoundException.class, () -> Class.forName("dev.langchain4j.http.client.jdk.JdkHttpClientBuilder"));
            assertEquals(1, requests.size());
        } finally {
            server.stop(0);
        }
    }
}
