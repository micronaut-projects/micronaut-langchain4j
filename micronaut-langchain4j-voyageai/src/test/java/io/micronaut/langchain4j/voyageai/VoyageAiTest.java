package io.micronaut.langchain4j.voyageai;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.scoring.ScoringModel;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The scoring and embedding models, against a fake endpoint that returns the API's response format.
 */
class VoyageAiTest {

    private static final String RERANK = """
        {"object":"list","data":[{"index":1,"relevance_score":0.1},{"index":0,"relevance_score":0.9}],"model":"test","usage":{"total_tokens":1}}
        """;
    private static final String EMBED = """
        {"object":"list","data":[{"object":"embedding","index":0,"embedding":[0.1,0.2]}],"model":"test","usage":{"total_tokens":1}}
        """;

    private final List<String> paths = new CopyOnWriteArrayList<>();
    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            exchange.getRequestBody().readAllBytes();
            String path = exchange.getRequestURI().getPath();
            paths.add(path);
            byte[] response = (path.endsWith("rerank") ? RERANK : EMBED).getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void scoresAndEmbedsWithTheMicronautHttpClient() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.voyage-ai.api-key", "test",
            "langchain4j.voyage-ai.base-url", "http://localhost:" + server.getAddress().getPort() + "/",
            "langchain4j.voyage-ai.scoring-model.model-name", "test",
            "langchain4j.voyage-ai.embedding-model.model-name", "test"))) {

            List<Double> scores = context.getBean(ScoringModel.class)
                .scoreAll(List.of(TextSegment.from("Micronaut"), TextSegment.from("Other")), "framework?")
                .content();
            assertEquals(List.of(0.9, 0.1), scores);

            float[] vector = context.getBean(EmbeddingModel.class).embed("Micronaut").content().vector();
            assertArrayEquals(new float[] {0.1f, 0.2f}, vector);
            assertTrue(paths.stream().anyMatch(path -> path.endsWith("rerank")), paths::toString);
        }
    }

    @Test
    void createsTheModelsWithTheDefaultBaseUrl() {
        try (ApplicationContext context = ApplicationContext.run(Map.of(
            "langchain4j.voyage-ai.api-key", "test",
            "langchain4j.voyage-ai.scoring-model.model-name", "test",
            "langchain4j.voyage-ai.embedding-model.model-name", "test"))) {
            context.getBean(ScoringModel.class);
            context.getBean(EmbeddingModel.class);
        }
    }
}
