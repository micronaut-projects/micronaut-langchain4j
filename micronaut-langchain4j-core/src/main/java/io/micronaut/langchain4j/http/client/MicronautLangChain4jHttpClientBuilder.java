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

import dev.langchain4j.http.client.HttpClient;
import dev.langchain4j.http.client.HttpClientBuilder;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.body.ByteBodyFactory;
import io.micronaut.http.client.HttpClientRegistry;
import io.micronaut.http.client.RawHttpClient;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

/**
 * Builds LangChain4j HTTP clients backed by Micronaut HTTP Client.
 */
@Internal
@Prototype
@Secondary
@Requires(classes = {HttpClient.class, RawHttpClient.class})
@Bean(typed = HttpClientBuilder.class)
final class MicronautLangChain4jHttpClientBuilder implements HttpClientBuilder {
    private final @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider;
    private final @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider;
    private final @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider;
    private final @Nullable BeanProvider<ExecutorService> blockingExecutorProvider;
    private final @Nullable BeanContext beanContext;
    private Duration connectTimeout;
    private Duration readTimeout;

    MicronautLangChain4jHttpClientBuilder() {
        this(null, null, null, null, null);
    }

    @Inject
    MicronautLangChain4jHttpClientBuilder(
        @Nullable BeanProvider<io.micronaut.http.client.HttpClient> httpClientProvider,
        @Nullable BeanProvider<HttpClientRegistry<io.micronaut.http.client.HttpClient>> httpClientRegistryProvider,
        @Nullable BeanProvider<ByteBodyFactory> byteBodyFactoryProvider,
        @Named(TaskExecutors.BLOCKING) @Nullable BeanProvider<ExecutorService> blockingExecutorProvider,
        @Nullable BeanContext beanContext) {
        this.httpClientProvider = httpClientProvider;
        this.httpClientRegistryProvider = httpClientRegistryProvider;
        this.byteBodyFactoryProvider = byteBodyFactoryProvider;
        this.blockingExecutorProvider = blockingExecutorProvider;
        this.beanContext = beanContext;
    }

    @Override
    public Duration connectTimeout() {
        return connectTimeout;
    }

    @Override
    public MicronautLangChain4jHttpClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public Duration readTimeout() {
        return readTimeout;
    }

    @Override
    public MicronautLangChain4jHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public HttpClient build() {
        return new MicronautLangChain4jHttpClient(
            httpClientProvider,
            httpClientRegistryProvider,
            byteBodyFactoryProvider,
            blockingExecutorProvider,
            beanContext,
            connectTimeout,
            readTimeout
        );
    }
}
