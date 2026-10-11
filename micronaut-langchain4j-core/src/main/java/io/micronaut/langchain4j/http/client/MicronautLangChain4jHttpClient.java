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

import dev.langchain4j.exception.HttpException;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.HttpResponseReceived;
import dev.langchain4j.http.client.sse.HttpStreamingEvent;
import dev.langchain4j.http.client.sse.ServerSentEventContext;
import dev.langchain4j.http.client.sse.ServerSentEventParsingHandle;
import dev.langchain4j.http.client.sse.DefaultServerSentEventParser;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.io.buffer.ByteArrayBufferFactory;
import io.micronaut.http.ByteBodyHttpResponse;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.body.ByteBody;
import io.micronaut.http.body.ByteBodyFactory;
import io.micronaut.http.body.CloseableAvailableByteBody;
import io.micronaut.http.body.CloseableByteBody;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.RawHttpClient;
import io.micronaut.http.client.exceptions.ReadTimeoutException;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.client.sse.SseClient;
import io.micronaut.http.sse.Event;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.jspecify.annotations.Nullable;
import reactor.adapter.JdkFlowAdapter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

final class MicronautLangChain4jHttpClient implements dev.langchain4j.http.client.HttpClient {
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(60);
    private static final AtomicInteger FALLBACK_EXECUTOR_SEQUENCE = new AtomicInteger();
    private static final Executor FALLBACK_SSE_EXECUTOR = runnable -> {
        Thread thread = new Thread(runnable, "micronaut-langchain4j-sse-" + FALLBACK_EXECUTOR_SEQUENCE.incrementAndGet());
        thread.setDaemon(true);
        thread.start();
    };

    private final @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider;
    private final @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider;
    private final @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider;
    private final @Nullable BeanProvider<ExecutorService> blockingExecutorProvider;
    private final @Nullable BeanContext beanContext;
    private final @Nullable BeanProvider<ModelAuthProvider> authProviders;
    private final @Nullable Duration connectTimeout;
    private final @Nullable Duration readTimeout;
    private final Map<URI, io.micronaut.http.client.HttpClient> configuredManagedClients = new ConcurrentHashMap<>();
    private final AtomicReference<ByteBodyFactory> fallbackByteBodyFactory = new AtomicReference<>();

    MicronautLangChain4jHttpClient(
        @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider,
        @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider,
        @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider,
        @Nullable BeanProvider<ExecutorService> blockingExecutorProvider,
        @Nullable BeanContext beanContext,
        @Nullable BeanProvider<ModelAuthProvider> authProviders,
        @Nullable Duration connectTimeout,
        @Nullable Duration readTimeout) {
        this.httpClientProvider = httpClientProvider;
        this.httpClientRegistryProvider = httpClientRegistryProvider;
        this.byteBodyFactoryProvider = byteBodyFactoryProvider;
        this.blockingExecutorProvider = blockingExecutorProvider;
        this.beanContext = beanContext;
        this.authProviders = authProviders;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public SuccessfulHttpResponse execute(HttpRequest modelRequest) throws HttpException, RuntimeException {
        HttpRequest request = authorize(modelRequest);
        try (ClientHandle client = client(request.url())) {
            io.micronaut.http.HttpResponse<?> response = exchange(client, request);
            try {
                String responseBody = readBody(response);
                if (!isSuccessful(response)) {
                    throw new HttpException(response.code(), responseBody);
                }
                return successfulResponse(response, responseBody);
            } finally {
                closeResponse(response);
            }
        } catch (ReadTimeoutException e) {
            throw new TimeoutException(e);
        } catch (IOException e) {
            throw new LangChain4jException("Error closing Micronaut HTTP client", e);
        }
    }

    /**
     * Executes the request without blocking a thread: the response is read by the Micronaut HTTP client's event loop.
     * Used by the non-blocking chat models (and the asynchronous AI services) of LangChain4j.
     */
    @Override
    public CompletableFuture<SuccessfulHttpResponse> executeAsync(HttpRequest modelRequest) {
        HttpRequest request = authorize(modelRequest);
        ClientHandle client;
        try {
            client = client(request.url());
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
        CompletableFuture<SuccessfulHttpResponse> result = new CompletableFuture<>();
        Mono<io.micronaut.http.HttpResponse<String>> exchange = Mono.from(client.httpClient().exchange(
                standardRequest(request),
                Argument.STRING,
                Argument.STRING))
            .onErrorResume(HttpClientResponseException.class, e -> Mono.just(stringResponse(e)));
        if (readTimeout != null) {
            exchange = exchange.timeout(readTimeout);
        }
        Disposable subscription = exchange
            .doFinally(signal -> closeQuietly(client))
            .subscribe(
                response -> {
                    String body = response.getBody(String.class).orElse(null);
                    if (isSuccessful(response)) {
                        result.complete(successfulResponse(response, body));
                    } else {
                        result.completeExceptionally(new HttpException(response.code(), body));
                    }
                },
                error -> result.completeExceptionally(error instanceof ReadTimeoutException || error instanceof java.util.concurrent.TimeoutException
                    ? new TimeoutException(error)
                    : error),
                () -> {
                    if (!result.isDone()) {
                        result.completeExceptionally(new LangChain4jException("HTTP client completed without response"));
                    }
                });
        result.whenComplete((response, error) -> {
            if (result.isCancelled()) {
                subscription.dispose();
            }
        });
        return result;
    }

    /**
     * Exposes the streamed response as a cold publisher: each subscription executes the request, emits an
     * {@link HttpResponseReceived} event followed by the parsed events, and cancelling the subscription cancels the
     * request.
     */
    @Override
    public Flow.Publisher<HttpStreamingEvent> stream(HttpRequest modelRequest, ServerSentEventParser parser) {
        HttpRequest request = authorize(modelRequest);
        Flux<HttpStreamingEvent> events = Flux.create(sink -> {
            AtomicReference<ServerSentEventParsingHandle> handle = new AtomicReference<>();
            sink.onCancel(() -> {
                ServerSentEventParsingHandle parsingHandle = handle.get();
                if (parsingHandle != null) {
                    parsingHandle.cancel();
                }
            });
            execute(request, parser, new ServerSentEventListener() {
                @Override
                public void onOpen(SuccessfulHttpResponse response) {
                    sink.next(new HttpResponseReceived(response));
                }

                @Override
                public void onEvent(dev.langchain4j.http.client.sse.ServerSentEvent event, ServerSentEventContext context) {
                    handle.compareAndSet(null, context.parsingHandle());
                    sink.next(event);
                }

                @Override
                public void onEvent(dev.langchain4j.http.client.sse.ServerSentEvent event) {
                    sink.next(event);
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }

                @Override
                public void onClose() {
                    sink.complete();
                }
            });
        });
        return JdkFlowAdapter.publisherToFlowPublisher(events);
    }

    private static io.micronaut.http.HttpResponse<String> stringResponse(HttpClientResponseException e) {
        io.micronaut.http.HttpResponse<?> response = e.getResponse();
        String body = readBody(response);
        return io.micronaut.http.HttpResponse.<String>status(response.code(), response.reason())
            .headers(headers -> response.getHeaders().forEach((name, values) -> values.forEach(value -> headers.add(name, value))))
            .body(body);
    }

    @Override
    public void execute(HttpRequest modelRequest, ServerSentEventParser parser, ServerSentEventListener listener) {
        HttpRequest request = authorize(modelRequest);
        CompletableFuture.runAsync(() -> {
            try {
                ClientHandle client = client(request.url());
                // the SSE client parses standard server-sent events: a custom parser (Ollama streams NDJSON) needs the raw response
                if (client.sseClient() != null && parser instanceof DefaultServerSentEventParser) {
                    stream(client, request, listener);
                    return;
                }
                try (client) {
                    io.micronaut.http.HttpResponse<?> response = rawExchange(client.rawHttpClient(), request);
                    try {
                        if (!isSuccessful(response)) {
                            safeOnError(listener, new HttpException(response.code(), readBody(response)));
                            return;
                        }
                        safeOnOpen(listener, successfulResponse(response, null));
                        parseBody(response, parser, listener);
                        safeOnClose(listener);
                    } finally {
                        closeResponse(response);
                    }
                }
            } catch (ReadTimeoutException e) {
                safeOnError(listener, new TimeoutException(e));
            } catch (Exception e) {
                safeOnError(listener, e);
            }
        }, sseExecutor());
    }

    /**
     * Applies the first credentials returned by the {@link ModelAuthProvider} beans, on the thread that sends the
     * request so that they can read the context of the current HTTP request.
     */
    private HttpRequest authorize(HttpRequest request) {
        if (authProviders == null) {
            return request;
        }
        ModelAuthRequest authRequest = null;
        for (ModelAuthProvider authProvider : authProviders) {
            if (authRequest == null) {
                authRequest = new ModelAuthRequest(request.method().name(), URI.create(request.url()), request.headers());
            }
            String authorization = authProvider.authorization(authRequest);
            if (authorization != null) {
                Map<String, List<String>> headers = new LinkedHashMap<>();
                request.headers().forEach((name, values) -> {
                    if (!HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                        headers.put(name, values);
                    }
                });
                headers.put(HttpHeaders.AUTHORIZATION, List.of(authorization));
                return HttpRequest.builder()
                    .method(request.method())
                    .url(request.url())
                    .headers(headers)
                    .formDataFields(request.formDataFields())
                    .formDataFiles(request.formDataFiles())
                    .body(request.body())
                    .build();
            }
        }
        return request;
    }

    private ClientHandle client(String url) {
        URI origin = origin(url);
        io.micronaut.http.client.HttpClient httpClient = managedHttpClient(origin);
        if (httpClient != null) {
            return new ClientHandle(
                httpClient,
                httpClient instanceof RawHttpClient rawHttpClient ? rawHttpClient : null,
                httpClient instanceof SseClient sseClient ? sseClient : null,
                false
            );
        }
        io.micronaut.http.client.HttpClient standaloneClient = createHttpClient(origin);
        return new ClientHandle(
            standaloneClient,
            standaloneClient instanceof RawHttpClient rawHttpClient ? rawHttpClient : null,
            standaloneClient instanceof SseClient sseClient ? sseClient : null,
            true
        );
    }

    private io.micronaut.http.client.@Nullable HttpClient managedHttpClient(URI origin) {
        if (hasConfiguredTimeout()) {
            io.micronaut.http.client.HttpClient configuredHttpClient = configuredManagedHttpClient(origin);
            if (configuredHttpClient != null) {
                return configuredHttpClient;
            }
        }
        if (httpClientProvider == null) {
            return null;
        }
        try {
            if (!httpClientProvider.isResolvable()) {
                return null;
            }
            return httpClientProvider.get();
        } catch (BeanContextException _) {
            return null;
        }
    }

    private io.micronaut.http.client.@Nullable HttpClient configuredManagedHttpClient(URI origin) {
        if (httpClientRegistryProvider == null || beanContext == null) {
            return null;
        }
        try {
            if (!httpClientRegistryProvider.isResolvable()) {
                return null;
            }
            return configuredManagedClients.computeIfAbsent(origin, this::resolveConfiguredManagedRawHttpClient);
        } catch (BeanContextException | IllegalStateException _) {
            return null;
        }
    }

    private io.micronaut.http.client.HttpClient resolveConfiguredManagedRawHttpClient(URI origin) {
        return httpClientRegistryProvider.get()
            .resolveClient(null, LoadBalancer.fixed(origin), configuration(), beanContext);
    }

    private boolean hasConfiguredTimeout() {
        return connectTimeout != null || readTimeout != null;
    }

    private io.micronaut.http.client.HttpClient createHttpClient(URI origin) {
        try {
            return io.micronaut.http.client.HttpClient.create(origin.toURL(), configuration());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid request origin: " + origin, e);
        }
    }

    private io.micronaut.http.HttpResponse<?> exchange(ClientHandle client, HttpRequest request) {
        RawHttpClient rawHttpClient = client.rawHttpClient();
        if (isMultipart(request) || rawHttpClient == null) {
            return standardExchange(client.httpClient(), request);
        }
        return rawExchange(rawHttpClient, request);
    }

    private io.micronaut.http.HttpResponse<?> rawExchange(RawHttpClient client, HttpRequest request) {
        MutableHttpRequest<Object> micronautRequest = micronautRequest(request);
        try (CloseableByteBody requestBody = body(request)) {
            Publisher<? extends io.micronaut.http.HttpResponse<?>> responsePublisher =
                client.exchange(micronautRequest, requestBody, Thread.currentThread());
            return block(responsePublisher, readTimeout);
        }
    }

    private io.micronaut.http.HttpResponse<?> standardExchange(io.micronaut.http.client.HttpClient client, HttpRequest request) {
        try {
            return block(client.exchange(
                standardRequest(request),
                Argument.STRING,
                Argument.STRING
            ), readTimeout);
        } catch (HttpClientResponseException e) {
            return e.getResponse();
        }
    }

    private void stream(ClientHandle client, HttpRequest request, ServerSentEventListener listener) {
        try {
            client.sseClient().eventStream(standardRequest(request), Argument.STRING)
                .subscribe(new SseSubscriber(listener, client));
        } catch (RuntimeException e) {
            closeQuietly(client);
            throw e;
        }
    }

    private DefaultHttpClientConfiguration configuration() {
        DefaultHttpClientConfiguration configuration = new DefaultHttpClientConfiguration();
        configuration.setExceptionOnErrorStatus(false);
        if (connectTimeout != null) {
            configuration.setConnectTimeout(connectTimeout);
        }
        if (readTimeout != null) {
            configuration.setReadTimeout(readTimeout);
            configuration.setRequestTimeout(readTimeout);
        }
        return configuration;
    }

    private static MutableHttpRequest<Object> micronautRequest(HttpRequest request) {
        MutableHttpRequest<Object> micronautRequest = io.micronaut.http.HttpRequest.create(
            io.micronaut.http.HttpMethod.valueOf(request.method().name()),
            request.url()
        );
        request.headers().forEach((name, values) -> {
            if (values != null) {
                values.stream()
                    .filter(Objects::nonNull)
                    .forEach(value -> micronautRequest.getHeaders().add(name, value));
            }
        });
        return micronautRequest;
    }

    private static MutableHttpRequest<?> standardRequest(HttpRequest request) {
        MutableHttpRequest<Object> micronautRequest = micronautRequest(request);
        if (isMultipart(request)) {
            return micronautRequest
                .contentType(MediaType.MULTIPART_FORM_DATA_TYPE)
                .body(multipartBody(request));
        }
        if (request.body() != null) {
            return micronautRequest.body(request.body());
        }
        return micronautRequest;
    }

    private @Nullable CloseableByteBody body(HttpRequest request) {
        if (request.body() == null) {
            return null;
        }
        return byteBodyFactory().copyOf(request.body(), StandardCharsets.UTF_8);
    }

    private ByteBodyFactory byteBodyFactory() {
        if (byteBodyFactoryProvider != null) {
            try {
                if (byteBodyFactoryProvider.isResolvable()) {
                    return byteBodyFactoryProvider.get();
                }
            } catch (BeanContextException _) {
                // Fall back to a standalone factory for manually constructed clients.
            }
        }
        return fallbackByteBodyFactory.updateAndGet(byteBodyFactory ->
            byteBodyFactory == null ? ByteBodyFactory.createDefault(ByteArrayBufferFactory.INSTANCE) : byteBodyFactory
        );
    }

    private Executor sseExecutor() {
        if (blockingExecutorProvider != null) {
            try {
                if (blockingExecutorProvider.isResolvable()) {
                    return blockingExecutorProvider.get();
                }
            } catch (BeanContextException _) {
                // Fall back for manually constructed clients or contexts without the blocking executor.
            }
        }
        return FALLBACK_SSE_EXECUTOR;
    }

    private static MultipartBody multipartBody(HttpRequest request) {
        MultipartBody.Builder builder = MultipartBody.builder();
        for (Map.Entry<String, String> entry : request.formDataFields().entrySet()) {
            builder.addPart(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, FormDataFile> entry : request.formDataFiles().entrySet()) {
            FormDataFile file = entry.getValue();
            builder.addPart(entry.getKey(), file.fileName(), contentType(file), file.content());
        }
        return builder.build();
    }

    private static MediaType contentType(FormDataFile file) {
        return file.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE : MediaType.of(file.contentType());
    }

    private static boolean isMultipart(HttpRequest request) {
        return !request.formDataFields().isEmpty() || !request.formDataFiles().isEmpty();
    }

    private static URI origin(String url) {
        URI uri = URI.create(url);
        return URI.create(uri.getScheme() + "://" + uri.getRawAuthority());
    }

    private static boolean isSuccessful(io.micronaut.http.HttpResponse<?> response) {
        int statusCode = response.code();
        return statusCode >= 200 && statusCode < 300;
    }

    private static SuccessfulHttpResponse successfulResponse(io.micronaut.http.HttpResponse<?> response, @Nullable String body) {
        return SuccessfulHttpResponse.builder()
            .statusCode(response.code())
            .headers(headers(response))
            .body(body)
            .build();
    }

    private static Map<String, List<String>> headers(io.micronaut.http.HttpResponse<?> response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.getHeaders().forEach((name, values) -> headers.put(name.toLowerCase(Locale.ROOT), List.copyOf(values)));
        return headers;
    }

    private static @Nullable String readBody(io.micronaut.http.HttpResponse<?> response) {
        @Nullable Object body = body(response);
        if (body == null) {
            return null;
        }
        if (body instanceof ByteBody byteBody) {
            try (CloseableAvailableByteBody buffered = byteBody.buffer().get()) {
                return buffered.toString(StandardCharsets.UTF_8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new LangChain4jException("Interrupted while reading response body", e);
            } catch (ExecutionException e) {
                throw new LangChain4jException("Error reading response body", e);
            }
        }
        if (body instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return body.toString();
    }

    private static void parseBody(
        io.micronaut.http.HttpResponse<?> response,
        ServerSentEventParser parser,
        ServerSentEventListener listener) {
        @Nullable Object body = body(response);
        if (body instanceof ByteBody byteBody) {
            try (InputStream inputStream = byteBody.toInputStream()) {
                parser.parse(inputStream, listener);
            } catch (IOException e) {
                throw new LangChain4jException("Error reading server-sent event response", e);
            }
        } else if (body != null) {
            parser.parse(new java.io.ByteArrayInputStream(body.toString().getBytes(StandardCharsets.UTF_8)), listener);
        }
    }

    private static @Nullable Object body(io.micronaut.http.HttpResponse<?> response) {
        if (response instanceof ByteBodyHttpResponse<?> byteBodyHttpResponse) {
            return byteBodyHttpResponse.byteBody();
        }
        return response.body();
    }

    private static void closeResponse(io.micronaut.http.HttpResponse<?> response) {
        if (response instanceof ByteBodyHttpResponse<?> byteBodyHttpResponse) {
            byteBodyHttpResponse.close();
        }
    }

    private static void safeOnOpen(ServerSentEventListener listener, SuccessfulHttpResponse response) {
        try {
            listener.onOpen(response);
        } catch (Exception _) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static void safeOnClose(ServerSentEventListener listener) {
        try {
            listener.onClose();
        } catch (Exception _) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static void safeOnError(ServerSentEventListener listener, Throwable throwable) {
        try {
            listener.onError(throwable);
        } catch (Exception _) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static void closeQuietly(ClientHandle client) {
        try {
            client.close();
        } catch (IOException _) {
            // Ignore close failures after the response has already completed.
        }
    }

    private static <T> T block(Publisher<? extends T> publisher, @Nullable Duration readTimeout) {
        Duration timeout = readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout;
        try {
            return Objects.requireNonNull(Mono.from(publisher).block(timeout), "HTTP client completed without response");
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Timeout on blocking read")) {
                throw new TimeoutException("Request timed out after " + timeout, e);
            }
            throw e;
        }
    }

    private static final class SseSubscriber implements Subscriber<Event<String>>, ServerSentEventParsingHandle {
        private final ServerSentEventListener listener;
        private final ClientHandle client;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final ServerSentEventContext context = new ServerSentEventContext(this);
        private final AtomicReference<Subscription> subscription = new AtomicReference<>();

        private SseSubscriber(ServerSentEventListener listener, ClientHandle client) {
            this.listener = listener;
            this.client = client;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription.set(subscription);
            safeOnOpen(listener, SuccessfulHttpResponse.builder()
                .statusCode(200)
                .headers(Map.of())
                .build());
            subscription.request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(Event<String> event) {
            if (!cancelled.get()) {
                listener.onEvent(new dev.langchain4j.http.client.sse.ServerSentEvent(event.getName(), event.getData()), context);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            try {
                safeOnError(listener, toHttpException(throwable));
            } finally {
                closeQuietly(client);
            }
        }

        @Override
        public void onComplete() {
            try {
                if (!cancelled.get()) {
                    safeOnClose(listener);
                }
            } finally {
                closeQuietly(client);
            }
        }

        @Override
        public void cancel() {
            cancelled.set(true);
            Subscription currentSubscription = subscription.get();
            if (currentSubscription != null) {
                currentSubscription.cancel();
            }
            closeQuietly(client);
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        private Throwable toHttpException(Throwable throwable) {
            if (throwable instanceof HttpClientResponseException e) {
                return new HttpException(e.code(), readBody(e.getResponse()));
            }
            if (throwable instanceof ReadTimeoutException e) {
                return new TimeoutException(e);
            }
            return throwable;
        }
    }

    private record ClientHandle(
        io.micronaut.http.client.HttpClient httpClient,
        @Nullable RawHttpClient rawHttpClient,
        @Nullable SseClient sseClient,
        boolean closeClient) implements AutoCloseable {
        @Override
        public void close() throws IOException {
            if (closeClient) {
                httpClient.close();
            }
        }
    }
}
