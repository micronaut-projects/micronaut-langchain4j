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
package io.micronaut.langchain4j.googlegenai;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

/**
 * Creates the Google Gen AI {@link Client} of the models.
 *
 * <p>The {@link ClientOptions.Builder} and {@link HttpOptions.Builder} are beans, so that a
 * {@link io.micronaut.context.event.BeanCreatedEventListener} can customize them, for example to set a custom
 * {@code OkHttpClient} with interceptors.</p>
 */
@Factory
@Internal
@Requires(property = GoogleGenAiClientConfiguration.PREFIX)
final class GoogleGenAiClientFactory {

    @Prototype
    ClientOptions.Builder clientOptionsBuilder(GoogleGenAiClientConfiguration configuration) {
        ClientOptions.Builder builder = ClientOptions.builder();
        if (configuration.getMaxConnections() != null) {
            builder.maxConnections(configuration.getMaxConnections());
        }
        if (configuration.getMaxConnectionsPerHost() != null) {
            builder.maxConnectionsPerHost(configuration.getMaxConnectionsPerHost());
        }
        return builder;
    }

    @Prototype
    HttpOptions.Builder httpOptionsBuilder(GoogleGenAiClientConfiguration configuration) {
        HttpOptions.Builder builder = HttpOptions.builder();
        if (configuration.getBaseUrl() != null) {
            builder.baseUrl(configuration.getBaseUrl());
        }
        if (configuration.getApiVersion() != null) {
            builder.apiVersion(configuration.getApiVersion());
        }
        if (configuration.getTimeout() != null) {
            builder.timeout(Math.toIntExact(configuration.getTimeout().toMillis()));
        }
        return builder;
    }

    @Singleton
    @Bean(preDestroy = "close")
    Client client(GoogleGenAiClientConfiguration configuration,
                  ClientOptions.Builder clientOptions,
                  HttpOptions.Builder httpOptions,
                  @Nullable GoogleCredentials credentials) {
        Client.Builder builder = Client.builder()
            .clientOptions(clientOptions.build())
            .httpOptions(httpOptions.build());
        if (configuration.isVertexAi()) {
            builder.vertexAI(true);
            if (configuration.getProjectId() != null) {
                builder.project(configuration.getProjectId());
            }
            if (configuration.getLocation() != null) {
                builder.location(configuration.getLocation());
            }
            if (credentials != null) {
                builder.credentials(credentials);
            }
        }
        if (configuration.getApiKey() != null) {
            builder.apiKey(configuration.getApiKey());
        }
        return builder.build();
    }
}
