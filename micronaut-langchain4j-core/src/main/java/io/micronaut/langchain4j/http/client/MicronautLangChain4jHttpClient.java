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
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.http.client.FormDataFile;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import dev.langchain4j.http.client.sse.ServerSentEventListener;
import dev.langchain4j.http.client.sse.ServerSentEventContext;
import dev.langchain4j.http.client.sse.ServerSentEventParsingHandle;
import dev.langchain4j.http.client.sse.ServerSentEventParser;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.BeanContextException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.io.buffer.ByteArrayBufferFactory;
import io.micronaut.http.ByteBodyHttpResponse;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

final class MicronautLangChain4jHttpClient implements dev.langchain4j.http.client.HttpClient {
    private final @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider;
    private final @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider;
    private final @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider;
    private final @Nullable BeanContext beanContext;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Map<URI, io.micronaut.http.client.HttpClient> configuredManagedClients = new ConcurrentHashMap<>();
    private @Nullable ByteBodyFactory fallbackByteBodyFactory;

    MicronautLangChain4jHttpClient(
        @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider,
        @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider,
        @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider,
        @Nullable BeanContext beanContext,
        Duration connectTimeout,
        Duration readTimeout) {
        this.httpClientProvider = httpClientProvider;
        this.httpClientRegistryProvider = httpClientRegistryProvider;
        this.byteBodyFactoryProvider = byteBodyFactoryProvider;
        this.beanContext = beanContext;
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    @Override
    public SuccessfulHttpResponse execute(HttpRequest request) throws HttpException, RuntimeException {
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(HttpRequest request, ServerSentEventParser parser, ServerSentEventListener listener) {
        CompletableFuture.runAsync(() -> {
            try {
                ClientHandle client = client(request.url());
                if (client.sseClient() != null) {
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
            } catch (Throwable e) {
                safeOnError(listener, e);
            }
        });
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
        } catch (BeanContextException e) {
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
        } catch (BeanContextException | IllegalStateException e) {
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
        if (isMultipart(request) || client.rawHttpClient() == null) {
            return standardExchange(client.httpClient(), request);
        }
        return rawExchange(client.rawHttpClient(), request);
    }

    private io.micronaut.http.HttpResponse<?> rawExchange(RawHttpClient client, HttpRequest request) {
        MutableHttpRequest<Object> micronautRequest = micronautRequest(request);
        CloseableByteBody requestBody = body(request);
        Publisher<? extends io.micronaut.http.HttpResponse<?>> responsePublisher =
            client.exchange(micronautRequest, requestBody, Thread.currentThread());
        return BlockingSingleSubscriber.get(responsePublisher);
    }

    private io.micronaut.http.HttpResponse<?> standardExchange(io.micronaut.http.client.HttpClient client, HttpRequest request) {
        try {
            return BlockingSingleSubscriber.get(client.exchange(
                standardRequest(request),
                Argument.STRING,
                Argument.STRING
            ));
        } catch (HttpClientResponseException e) {
            return e.getResponse();
        }
    }

    private void stream(ClientHandle client, HttpRequest request, ServerSentEventListener listener) {
        try {
            client.sseClient().eventStream(micronautRequest(request), Argument.STRING)
                .subscribe(new SseSubscriber(listener, client));
        } catch (Throwable e) {
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
                    .filter(value -> value != null)
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

    private CloseableByteBody body(HttpRequest request) {
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
            } catch (BeanContextException ignored) {
                // Fall back to a standalone factory for manually constructed clients.
            }
        }
        ByteBodyFactory byteBodyFactory = fallbackByteBodyFactory;
        if (byteBodyFactory == null) {
            byteBodyFactory = ByteBodyFactory.createDefault(ByteArrayBufferFactory.INSTANCE);
            fallbackByteBodyFactory = byteBodyFactory;
        }
        return byteBodyFactory;
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

    private static SuccessfulHttpResponse successfulResponse(io.micronaut.http.HttpResponse<?> response, String body) {
        return SuccessfulHttpResponse.builder()
            .statusCode(response.code())
            .headers(headers(response))
            .body(body)
            .build();
    }

    private static Map<String, List<String>> headers(io.micronaut.http.HttpResponse<?> response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.getHeaders().forEach((name, values) -> headers.put(name, List.copyOf(values)));
        return headers;
    }

    private static String readBody(io.micronaut.http.HttpResponse<?> response) {
        Object body = body(response);
        if (body == null) {
            return null;
        }
        if (body instanceof ByteBody byteBody) {
            try (CloseableAvailableByteBody buffered = byteBody.buffer().get()) {
                return buffered.toString(StandardCharsets.UTF_8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
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
        Object body = body(response);
        if (body instanceof ByteBody byteBody) {
            try (InputStream inputStream = byteBody.toInputStream()) {
                parser.parse(inputStream, listener);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (body != null) {
            parser.parse(new java.io.ByteArrayInputStream(body.toString().getBytes(StandardCharsets.UTF_8)), listener);
        }
    }

    private static Object body(io.micronaut.http.HttpResponse<?> response) {
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
        } catch (Throwable ignored) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static void safeOnClose(ServerSentEventListener listener) {
        try {
            listener.onClose();
        } catch (Throwable ignored) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static void safeOnError(ServerSentEventListener listener, Throwable throwable) {
        try {
            listener.onError(throwable);
        } catch (Throwable ignored) {
            // Ignore listener exceptions to match LangChain4j HTTP client behavior.
        }
    }

    private static Throwable toHttpException(Throwable throwable) {
        if (throwable instanceof HttpClientResponseException e) {
            return new HttpException(e.code(), readBody(e.getResponse()));
        }
        if (throwable instanceof ReadTimeoutException e) {
            return new TimeoutException(e);
        }
        return throwable;
    }

    private static void closeQuietly(ClientHandle client) {
        try {
            client.close();
        } catch (IOException ignored) {
            // Ignore close failures after the response has already completed.
        }
    }

    private static final class SseSubscriber implements Subscriber<Event<String>>, ServerSentEventParsingHandle {
        private final ServerSentEventListener listener;
        private final ClientHandle client;
        private final AtomicBoolean cancelled = new AtomicBoolean();
        private final ServerSentEventContext context = new ServerSentEventContext(this);
        private volatile Subscription subscription;

        private SseSubscriber(ServerSentEventListener listener, ClientHandle client) {
            this.listener = listener;
            this.client = client;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
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
            Subscription currentSubscription = subscription;
            if (currentSubscription != null) {
                currentSubscription.cancel();
            }
            closeQuietly(client);
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }
    }

    private static final class BlockingSingleSubscriber<T> implements Subscriber<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicReference<T> value = new AtomicReference<>();
        private final AtomicReference<Throwable> error = new AtomicReference<>();

        static <T> T get(Publisher<? extends T> publisher) {
            BlockingSingleSubscriber<T> subscriber = new BlockingSingleSubscriber<>();
            publisher.subscribe(subscriber);
            try {
                subscriber.latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            Throwable throwable = subscriber.error.get();
            if (throwable != null) {
                if (throwable instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException(throwable);
            }
            return subscriber.value.get();
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            subscription.request(1);
        }

        @Override
        public void onNext(T value) {
            this.value.set(value);
        }

        @Override
        public void onError(Throwable throwable) {
            error.set(throwable);
            latch.countDown();
        }

        @Override
        public void onComplete() {
            latch.countDown();
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
