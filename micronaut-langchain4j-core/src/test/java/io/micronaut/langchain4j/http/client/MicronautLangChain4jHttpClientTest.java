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
package io.micronaut.langchain4j.http.client;

import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.exception.HttpException;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventContext;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicronautLangChain4jHttpClientTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void executesSuccessfulRequestsWithMicronautHttpClient() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/echo", exchange -> {
            byte[] requestBody = exchange.getRequestBody().readAllBytes();
            byte[] response = (exchange.getRequestMethod() + ":" + new String(requestBody, StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("X-Test", "ok");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        SuccessfulHttpResponse response = new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/echo"))
                .body("hello")
                .build());

        assertEquals(200, response.statusCode());
        assertEquals("POST:hello", response.body());
        assertEquals("ok", response.headers().get("X-test").get(0));
    }

    @Test
    void mapsErrorResponsesToLangChain4jHttpException() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/error", exchange -> {
            byte[] response = "bad request".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        HttpException exception = assertThrows(HttpException.class, () -> new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url(url("/error"))
                .build()));

        assertEquals(400, exception.statusCode());
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    void sendsMultipartFormDataRequests() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/multipart", exchange -> {
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(contentType.startsWith("multipart/form-data; boundary="));
            assertTrue(requestBody.contains("name=\"purpose\""));
            assertTrue(requestBody.contains("fine-tune"));
            assertTrue(requestBody.contains("filename=\"sample.txt\""));
            assertTrue(requestBody.contains("sample content"));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();

        SuccessfulHttpResponse response = new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/multipart"))
                .addFormDataField("purpose", "fine-tune")
                .addFormDataFile("file", "sample.txt", "text/plain", "sample content".getBytes(StandardCharsets.UTF_8))
                .build());

        assertEquals(204, response.statusCode());
    }

    @Test
    void executesServerSentEventRequests() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sse", exchange -> {
            byte[] response = """
                event: message
                data: hello

                event: message
                data: world

                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        AtomicBoolean opened = new AtomicBoolean();
        AtomicBoolean closed = new AtomicBoolean();
        AtomicReference<Throwable> error = new AtomicReference<>();
        List<String> events = new ArrayList<>();
        CountDownLatch closedLatch = new CountDownLatch(1);

        new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url(url("/sse"))
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onOpen(SuccessfulHttpResponse response) {
                        opened.set(true);
                    }

                    @Override
                    public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                        events.add(event.data());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        closedLatch.countDown();
                    }

                    @Override
                    public void onClose() {
                        closed.set(true);
                        closedLatch.countDown();
                    }
                });

        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertEquals(null, error.get());
        assertTrue(opened.get());
        assertTrue(closed.get());
        assertEquals(List.of("hello", "world"), events);
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }
}
