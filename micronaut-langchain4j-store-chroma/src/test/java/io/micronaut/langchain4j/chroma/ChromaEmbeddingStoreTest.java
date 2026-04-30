/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.chroma;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import io.micronaut.context.ApplicationContext;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ChromaEmbeddingStoreTest {

    @Test
    void testEmbeddingStoreBeanIsCreated() throws IOException {
        List<String> requests = Collections.synchronizedList(new ArrayList<>());
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> handleRequest(exchange, requests));
        server.start();
        try (ApplicationContext applicationContext = ApplicationContext.run(Map.of(
            "langchain4j.chroma.embedding-store.base-url", "http://localhost:" + server.getAddress().getPort(),
            "langchain4j.chroma.embedding-store.collection-name", "documents",
            "langchain4j.chroma.embedding-store.api-version", "V2",
            "langchain4j.chroma.embedding-store.tenant-name", "tenant",
            "langchain4j.chroma.embedding-store.database-name", "database"
        ))) {
            EmbeddingStore<?> embeddingStore = applicationContext.getBean(EmbeddingStore.class);

            Assertions.assertNotNull(embeddingStore);
            Assertions.assertInstanceOf(ChromaEmbeddingStore.class, embeddingStore);
            Assertions.assertEquals(
                List.of(
                    "GET /api/v2/tenants/tenant",
                    "POST /api/v2/tenants",
                    "GET /api/v2/tenants/tenant/databases/database",
                    "POST /api/v2/tenants/tenant/databases",
                    "GET /api/v2/tenants/tenant/databases/database/collections/documents",
                    "POST /api/v2/tenants/tenant/databases/database/collections"
                ),
                requests
            );
        } finally {
            server.stop(0);
        }
    }

    private static void handleRequest(HttpExchange exchange, List<String> requests) throws IOException {
        String route = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
        requests.add(route);
        switch (route) {
            case "GET /api/v2/tenants/tenant" -> sendResponse(exchange, 404, "{\"error\":\"missing tenant\"}");
            case "POST /api/v2/tenants" -> sendResponse(exchange, 200, "");
            case "GET /api/v2/tenants/tenant/databases/database" -> sendResponse(exchange, 404, "{\"error\":\"missing database\"}");
            case "POST /api/v2/tenants/tenant/databases" -> sendResponse(exchange, 200, "");
            case "GET /api/v2/tenants/tenant/databases/database/collections/documents" ->
                sendResponse(exchange, 404, "{\"error\":\"missing collection\"}");
            case "POST /api/v2/tenants/tenant/databases/database/collections" ->
                sendResponse(exchange, 200, "{\"id\":\"collection-1\",\"name\":\"documents\",\"metadata\":{\"hnsw:space\":\"cosine\"}}");
            default -> sendResponse(exchange, 500, "{\"error\":\"unexpected request\"}");
        }
    }

    private static void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
