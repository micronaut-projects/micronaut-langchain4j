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
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.HttpResponseReceived;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEvent;
import dev.langchain4j.http.client.sse.ServerSentEventContext;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import io.micronaut.core.io.buffer.ByteArrayBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.ByteBodyHttpResponse;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseWrapper;
import io.micronaut.http.body.ByteBody;
import io.micronaut.http.body.ByteBodyFactory;
import io.micronaut.http.body.CloseableByteBody;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.RawHttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.sse.Event;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        assertEquals("ok", response.headers().get("x-test").get(0));
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

        dev.langchain4j.http.client.HttpClient client = new MicronautLangChain4jHttpClientBuilder()
            .build();
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .url(url("/error"))
            .build();

        HttpException exception = assertThrows(HttpException.class, () -> client.execute(request));

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
    void sendsMultipartFilesWithoutContentType() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/multipart", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(requestBody.contains("filename=\"sample.bin\""));
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
                .addFormDataFile("file", "sample.bin", null, "sample content".getBytes(StandardCharsets.UTF_8))
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
        assertNull(error.get());
        assertTrue(opened.get());
        assertTrue(closed.get());
        assertEquals(List.of("hello", "world"), events);
    }

    @Test
    void executesRequestsAsynchronously() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/async", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = "{\"answer\":\"micronaut\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/async-error", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = "{\"error\":\"bad request\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(400, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        var client = new MicronautLangChain4jHttpClientBuilder().build();

        SuccessfulHttpResponse response = client.executeAsync(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/async"))
                .addHeader("Content-Type", "application/json")
                .body("{\"question\":\"framework?\"}")
                .build())
            .get(5, TimeUnit.SECONDS);

        assertEquals(200, response.statusCode());
        assertEquals("{\"answer\":\"micronaut\"}", response.body());
        assertEquals("{\"question\":\"framework?\"}", receivedBody.get());

        java.util.concurrent.ExecutionException failure = assertThrows(java.util.concurrent.ExecutionException.class, () -> client.executeAsync(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/async-error"))
                .body("{}")
                .build())
            .get(5, TimeUnit.SECONDS));
        HttpException httpException = assertInstanceOf(HttpException.class, failure.getCause());
        assertEquals(400, httpException.statusCode());
        assertEquals("{\"error\":\"bad request\"}", httpException.getMessage());
    }

    @Test
    void failsTheAsyncRequestsThatCannotBeSent() {
        java.util.concurrent.CompletableFuture<SuccessfulHttpResponse> future = new MicronautLangChain4jHttpClientBuilder()
            .build()
            .executeAsync(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("not a url")
                .build());
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    void timesOutAndCancelsAsyncRequests() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/slow", exchange -> {
            exchange.getRequestBody().readAllBytes();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        try {
            var client = new MicronautLangChain4jHttpClientBuilder()
                .readTimeout(java.time.Duration.ofMillis(200))
                .build();
            java.util.concurrent.ExecutionException failure = assertThrows(java.util.concurrent.ExecutionException.class,
                () -> client.executeAsync(slowRequest()).get(5, TimeUnit.SECONDS));
            assertInstanceOf(TimeoutException.class, failure.getCause());

            java.util.concurrent.CompletableFuture<SuccessfulHttpResponse> cancelled = new MicronautLangChain4jHttpClientBuilder()
                .build()
                .executeAsync(slowRequest());
            assertTrue(cancelled.cancel(true));
            assertTrue(cancelled.isCancelled());
        } finally {
            release.countDown();
        }
    }

    private HttpRequest slowRequest() {
        return HttpRequest.builder()
            .method(HttpMethod.POST)
            .url(url("/slow"))
            .body("{}")
            .build();
    }

    @Test
    void streamsTheEventsOfARequestParserAsAPublisher() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ndjson", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = "{\"part\":\"micro\"}\n{\"part\":\"naut\"}\n".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/x-ndjson");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/failing", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = "boom".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        ServerSentEventParser lineParser = (inputStream, listener) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> listener.onEvent(new ServerSentEvent(null, line)));
            } catch (IOException e) {
                listener.onError(e);
            }
        };
        var client = new MicronautLangChain4jHttpClientBuilder().build();

        List<HttpStreamingEvent> events = reactor.adapter.JdkFlowAdapter.flowPublisherToFlux(client.stream(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/ndjson"))
                .body("{}")
                .build(), lineParser))
            .collectList()
            .block(java.time.Duration.ofSeconds(5));
        assertEquals(List.of("{\"part\":\"micro\"}", "{\"part\":\"naut\"}"), events.stream()
            .filter(ServerSentEvent.class::isInstance)
            .map(event -> ((ServerSentEvent) event).data())
            .toList());

        reactor.core.publisher.Flux<HttpStreamingEvent> failing = reactor.adapter.JdkFlowAdapter.flowPublisherToFlux(client.stream(HttpRequest.builder()
            .method(HttpMethod.POST)
            .url(url("/failing"))
            .body("{}")
            .build(), lineParser));
        HttpException error = assertThrows(HttpException.class, () -> failing.blockLast(java.time.Duration.ofSeconds(5)));
        assertEquals(500, error.statusCode());
    }

    @Test
    void streamsEventsAsAPublisher() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sse", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = """
                data: micro

                data: naut

                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        java.util.concurrent.Flow.Publisher<HttpStreamingEvent> publisher = new MicronautLangChain4jHttpClientBuilder()
            .build()
            .stream(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/sse"))
                .body("{}")
                .build());
        List<HttpStreamingEvent> events = reactor.adapter.JdkFlowAdapter.flowPublisherToFlux(publisher)
            .collectList()
            .block(java.time.Duration.ofSeconds(5));

        assertInstanceOf(HttpResponseReceived.class, events.getFirst());
        assertEquals(List.of("micro", "naut"), events.stream()
            .filter(ServerSentEvent.class::isInstance)
            .map(event -> ((ServerSentEvent) event).data())
            .toList());
    }

    @Test
    void sendsTheBodyOfServerSentEventRequests() throws Exception {
        AtomicReference<String> receivedBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/sse", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] response = """
                data: done

                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch closedLatch = new CountDownLatch(1);

        new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/sse"))
                .addHeader("Content-Type", "application/json")
                .body("{\"stream\":true}")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                        // the events are not checked: the test only checks that the request body reaches the server
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        closedLatch.countDown();
                    }

                    @Override
                    public void onClose() {
                        closedLatch.countDown();
                    }
                });

        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals("{\"stream\":true}", receivedBody.get());
    }

    @Test
    void usesTheParserOfTheRequestForNonServerSentEventStreams() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ndjson", exchange -> {
            exchange.getRequestBody().readAllBytes();
            byte[] response = "{\"part\":\"micro\"}\n{\"part\":\"naut\"}\n".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/x-ndjson");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();

        // like Ollama's parser: one event per line, no SSE framing
        ServerSentEventParser lineParser = (inputStream, listener) -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> listener.onEvent(new ServerSentEvent(null, line)));
            } catch (IOException e) {
                listener.onError(e);
            }
        };
        List<String> events = new CopyOnWriteArrayList<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch closedLatch = new CountDownLatch(1);

        new MicronautLangChain4jHttpClientBuilder()
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.POST)
                .url(url("/ndjson"))
                .addHeader("Content-Type", "application/json")
                .body("{}")
                .build(), lineParser, new ServerSentEventListener() {
                    @Override
                    public void onEvent(ServerSentEvent event) {
                        events.add(event.data());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        closedLatch.countDown();
                    }

                    @Override
                    public void onClose() {
                        closedLatch.countDown();
                    }
                });

        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(List.of("{\"part\":\"micro\"}", "{\"part\":\"naut\"}"), events);
    }

    @Test
    void parsesServerSentEventsWithoutSseClient() throws Exception {
        RawOnlyHttpClient rawClient = new RawOnlyHttpClient(single(HttpResponse.ok("""
            data: micro

            data: naut

            """)));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch closedLatch = new CountDownLatch(1);
        List<String> events = new ArrayList<>();

        client(rawClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse")
                .build(), new ServerSentEventListener() {
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
                        closedLatch.countDown();
                    }
                });

        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(List.of("micro", "naut"), events);
    }

    @Test
    void parsesServerSentEventsFromByteBodyResponsesWithoutSseClient() throws Exception {
        AtomicBoolean closed = new AtomicBoolean();
        CountDownLatch responseClosedLatch = new CountDownLatch(1);
        RawOnlyHttpClient rawClient = new RawOnlyHttpClient(single(byteBodyResponse("""
            data: byte

            data: body

            """, closed, responseClosedLatch)));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch closedLatch = new CountDownLatch(1);
        List<String> events = new ArrayList<>();

        client(rawClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse")
                .build(), new ServerSentEventListener() {
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
                        closedLatch.countDown();
                    }
                });

        assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertEquals(List.of("byte", "body"), events);
        assertTrue(responseClosedLatch.await(5, TimeUnit.SECONDS));
        assertTrue(closed.get());
    }

    @Test
    void ignoresFallbackServerSentEventOpenAndCloseListenerExceptions() throws Exception {
        RawOnlyHttpClient rawClient = new RawOnlyHttpClient(single(HttpResponse.ok("""
            data: tolerated

            """)));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch errorLatch = new CountDownLatch(1);
        CountDownLatch eventLatch = new CountDownLatch(1);
        CountDownLatch closeLatch = new CountDownLatch(1);
        List<String> events = new ArrayList<>();

        client(rawClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onOpen(SuccessfulHttpResponse response) {
                        throw new IllegalStateException("open");
                    }

                    @Override
                    public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                        events.add(event.data());
                        eventLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        errorLatch.countDown();
                        eventLatch.countDown();
                    }

                    @Override
                    public void onClose() {
                        closeLatch.countDown();
                        throw new IllegalStateException("close");
                    }
                });

        assertTrue(eventLatch.await(5, TimeUnit.SECONDS));
        assertTrue(closeLatch.await(5, TimeUnit.SECONDS));
        assertFalse(errorLatch.await(100, TimeUnit.MILLISECONDS));
        assertNull(error.get());
        assertEquals(List.of("tolerated"), events);
    }

    @Test
    void mapsAsyncErrorResponsesWithoutSseClient() throws Exception {
        RawOnlyHttpClient rawClient = new RawOnlyHttpClient(single(HttpResponse.badRequest("bad request")));
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch errorLatch = new CountDownLatch(1);

        client(rawClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/error")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        errorLatch.countDown();
                        throw new IllegalStateException("ignored");
                    }
                });

        assertTrue(errorLatch.await(5, TimeUnit.SECONDS));
        assertTrue(error.get() instanceof HttpException);
        assertEquals(400, ((HttpException) error.get()).statusCode());
        assertEquals("bad request", error.get().getMessage());
    }

    @Test
    void readsByteArrayResponseBodies() {
        dev.langchain4j.http.client.HttpClient client = client(
            new RawOnlyHttpClient(single(HttpResponse.ok("bytes".getBytes(StandardCharsets.UTF_8)))),
            java.time.Duration.ofSeconds(1)
        );
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .url("http://localhost/bytes")
            .build();

        SuccessfulHttpResponse response = client.execute(request);

        assertEquals("bytes", response.body());
    }

    @Test
    void readsByteBodyResponseBodiesAndClosesResponses() {
        AtomicBoolean closed = new AtomicBoolean();
        dev.langchain4j.http.client.HttpClient client = client(
            new RawOnlyHttpClient(single(byteBodyResponse("byte-body", closed))),
            java.time.Duration.ofSeconds(1)
        );
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .url("http://localhost/byte-body")
            .build();

        SuccessfulHttpResponse response = client.execute(request);

        assertEquals("byte-body", response.body());
        assertTrue(closed.get());
    }

    @Test
    void executesRequestsWithStandardHttpClients() {
        AtomicReference<io.micronaut.http.HttpRequest<?>> received = new AtomicReference<>();
        StandardOnlyHttpClient standardClient = new StandardOnlyHttpClient(request -> {
            received.set(request);
            return single(HttpResponse.ok("standard").header("X-Test", "ok"));
        });
        dev.langchain4j.http.client.HttpClient client = client(standardClient, java.time.Duration.ofSeconds(1));
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.POST)
            .url("http://localhost/standard")
            .addHeader("X-Input", "present")
            .body("payload")
            .build();

        SuccessfulHttpResponse response = client.execute(request);

        assertEquals(200, response.statusCode());
        assertEquals("standard", response.body());
        assertEquals("ok", response.headers().get("x-test").get(0));
        assertEquals("present", received.get().getHeaders().get("X-Input"));
        assertEquals("payload", received.get().getBody(String.class).orElseThrow());
    }

    @Test
    void mapsStandardClientErrorResponsesToLangChain4jHttpException() {
        StandardOnlyHttpClient standardClient = new StandardOnlyHttpClient(_ -> {
            throw new HttpClientResponseException("Bad Request", HttpResponse.badRequest("bad request"));
        });
        dev.langchain4j.http.client.HttpClient client = client(standardClient, java.time.Duration.ofSeconds(1));
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .url("http://localhost/error")
            .build();

        HttpException exception = assertThrows(HttpException.class, () -> client.execute(request));

        assertEquals(400, exception.statusCode());
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    void mapsSseClientErrorsToHttpExceptions() throws Exception {
        SseHttpClient sseClient = new SseHttpClient(subscriber -> {
            subscriber.onSubscribe(new EmptySubscription());
            subscriber.onError(new HttpClientResponseException("Bad Request", HttpResponse.badRequest("bad request")));
        });
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch errorLatch = new CountDownLatch(1);

        client(sseClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse-error")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        errorLatch.countDown();
                    }
                });

        assertTrue(errorLatch.await(5, TimeUnit.SECONDS));
        assertTrue(error.get() instanceof HttpException);
        assertEquals(400, ((HttpException) error.get()).statusCode());
        assertEquals("bad request", error.get().getMessage());
    }

    @Test
    void cancelsSseClientSubscriptions() throws Exception {
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        AtomicBoolean cancelled = new AtomicBoolean();
        SseHttpClient sseClient = new SseHttpClient(subscriber -> {
            subscription.set(new Subscription() {
                @Override
                public void request(long n) {
                    subscriber.onNext(Event.of("stop"));
                }

                @Override
                public void cancel() {
                    cancelled.set(true);
                }
            });
            subscriber.onSubscribe(subscription.get());
        });
        CountDownLatch eventLatch = new CountDownLatch(1);
        AtomicReference<Throwable> error = new AtomicReference<>();

        client(sseClient, java.time.Duration.ofSeconds(1))
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse-cancel")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                        context.parsingHandle().cancel();
                        eventLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        error.set(throwable);
                        eventLatch.countDown();
                    }
                });

        assertTrue(eventLatch.await(5, TimeUnit.SECONDS));
        assertNull(error.get());
        assertTrue(cancelled.get());
    }

    @Test
    void timesOutWaitingForRawResponse() {
        dev.langchain4j.http.client.HttpClient client = client(
            new RawOnlyHttpClient(subscriber -> subscriber.onSubscribe(new EmptySubscription())),
            java.time.Duration.ofMillis(10)
        );
        HttpRequest request = HttpRequest.builder()
            .method(HttpMethod.GET)
            .url("http://localhost/never")
            .build();

        assertThrows(TimeoutException.class, () -> client.execute(request));
    }

    @Test
    void usesInjectedBlockingExecutorForServerSentEventFallback() throws Exception {
        try (ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> new Thread(runnable, "test-blocking-executor"))) {
            RawOnlyHttpClient rawClient = new RawOnlyHttpClient(single(HttpResponse.ok("""
                data: executor

                """)));
            AtomicReference<String> threadName = new AtomicReference<>();
            CountDownLatch closedLatch = new CountDownLatch(1);

            new MicronautLangChain4jHttpClient(
                () -> rawClient,
                null,
                null,
                () -> executorService,
                null,
                null,
                java.time.Duration.ofSeconds(1)
            ).execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url("http://localhost/sse")
                .build(), new ServerSentEventListener() {
                    @Override
                    public void onEvent(ServerSentEvent event, ServerSentEventContext context) {
                        threadName.set(Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        closedLatch.countDown();
                    }

                    @Override
                    public void onClose() {
                        closedLatch.countDown();
                    }
                });

            assertTrue(closedLatch.await(5, TimeUnit.SECONDS));
            assertEquals("test-blocking-executor", threadName.get());
        }
    }

    private String url(String path) {
        return "http://localhost:" + server.getAddress().getPort() + path;
    }

    private static dev.langchain4j.http.client.HttpClient client(RawOnlyHttpClient rawClient, java.time.Duration readTimeout) {
        return client((io.micronaut.http.client.HttpClient) rawClient, readTimeout);
    }

    private static dev.langchain4j.http.client.HttpClient client(
        io.micronaut.http.client.HttpClient httpClient,
        java.time.Duration readTimeout) {
        return new MicronautLangChain4jHttpClient(
            () -> httpClient,
            null,
            null,
            null,
            null,
            null,
            readTimeout
        );
    }

    private static ByteBodyHttpResponse<Object> byteBodyResponse(String value, AtomicBoolean closed) {
        return byteBodyResponse(value, closed, new CountDownLatch(0));
    }

    private static ByteBodyHttpResponse<Object> byteBodyResponse(
        String value,
        AtomicBoolean closed,
        CountDownLatch closedLatch) {
        ByteBody byteBody = ByteBodyFactory.createDefault(ByteArrayBufferFactory.INSTANCE)
            .copyOf(value, StandardCharsets.UTF_8);
        return new TestByteBodyResponse(HttpResponse.ok(), byteBody, closed, closedLatch);
    }

    private static <T> Publisher<T> single(T value) {
        return subscriber -> {
            subscriber.onSubscribe(new EmptySubscription());
            subscriber.onNext(value);
            subscriber.onComplete();
        };
    }

    private static final class EmptySubscription implements Subscription {
        @Override
        public void request(long n) {
            // Test publishers emit synchronously and do not need demand tracking.
        }

        @Override
        public void cancel() {
            // Cancellation has no observable effect for the immediate test publishers.
        }
    }

    private static class RawOnlyHttpClient implements io.micronaut.http.client.HttpClient, RawHttpClient {
        private final Publisher<? extends HttpResponse<?>> responsePublisher;

        private RawOnlyHttpClient(Publisher<? extends HttpResponse<?>> responsePublisher) {
            this.responsePublisher = responsePublisher;
        }

        @Override
        public Publisher<? extends HttpResponse<?>> exchange(
            io.micronaut.http.HttpRequest<?> request,
            CloseableByteBody requestBody,
            Thread blockedThread) {
            return responsePublisher;
        }

        @Override
        public BlockingHttpClient toBlocking() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public <I, O, E> Publisher<HttpResponse<O>> exchange(
            io.micronaut.http.HttpRequest<I> request,
            Argument<O> bodyType,
            Argument<E> errorType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            // The test double does not own any external resources.
        }
    }

    private static final class StandardOnlyHttpClient implements io.micronaut.http.client.HttpClient {
        private final Function<io.micronaut.http.HttpRequest<?>, Publisher<? extends HttpResponse<?>>> responsePublisher;

        private StandardOnlyHttpClient(Function<io.micronaut.http.HttpRequest<?>, Publisher<? extends HttpResponse<?>>> responsePublisher) {
            this.responsePublisher = responsePublisher;
        }

        @Override
        public BlockingHttpClient toBlocking() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public <I, O, E> Publisher<HttpResponse<O>> exchange(
            io.micronaut.http.HttpRequest<I> request,
            Argument<O> bodyType,
            Argument<E> errorType) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Publisher<HttpResponse<O>> publisher = (Publisher) responsePublisher.apply(request);
            return publisher;
        }

        @Override
        public void close() {
            // The test double does not own any external resources.
        }
    }

    private static final class TestByteBodyResponse extends HttpResponseWrapper<Object> implements ByteBodyHttpResponse<Object> {
        private final ByteBody byteBody;
        private final AtomicBoolean closed;
        private final CountDownLatch closedLatch;

        private TestByteBodyResponse(
            HttpResponse<Object> delegate,
            ByteBody byteBody,
            AtomicBoolean closed,
            CountDownLatch closedLatch) {
            super(delegate);
            this.byteBody = byteBody;
            this.closed = closed;
            this.closedLatch = closedLatch;
        }

        @Override
        public ByteBody byteBody() {
            return byteBody;
        }

        @Override
        public void close() {
            closed.set(true);
            closedLatch.countDown();
        }
    }

    private static final class SseHttpClient extends RawOnlyHttpClient implements SseClient {
        private final Publisher<Event<String>> eventPublisher;

        private SseHttpClient(Publisher<Event<String>> eventPublisher) {
            super(single(HttpResponse.ok()));
            this.eventPublisher = eventPublisher;
        }

        @Override
        public <I> Publisher<Event<io.micronaut.core.io.buffer.ByteBuffer<?>>> eventStream(io.micronaut.http.HttpRequest<I> request) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <I, B> Publisher<Event<B>> eventStream(io.micronaut.http.HttpRequest<I> request, Argument<B> eventType) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Publisher<Event<B>> publisher = (Publisher) eventPublisher;
            return publisher;
        }

        @Override
        public <I, B> Publisher<Event<B>> eventStream(
            io.micronaut.http.HttpRequest<I> request,
            Argument<B> eventType,
            Argument<?> errorType) {
            return eventStream(request, eventType);
        }
    }
}
