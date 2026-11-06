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

import io.micronaut.context.annotation.ConfigurationProperties;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Configures the Google Gen AI {@link com.google.genai.Client} shared by the Google Gen AI models: the Gemini API
 * with an API key, or Vertex AI with a project, a location and the application's {@code GoogleCredentials}.
 *
 * @since 2.4.0
 */
@ConfigurationProperties(GoogleGenAiClientConfiguration.PREFIX)
public final class GoogleGenAiClientConfiguration {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.google-gen-ai";

    private @Nullable String apiKey;
    private boolean vertexAi;
    private @Nullable String projectId;
    private @Nullable String location;
    private @Nullable String baseUrl;
    private @Nullable String apiVersion;
    private @Nullable Duration timeout;
    private @Nullable Integer maxConnections;
    private @Nullable Integer maxConnectionsPerHost;

    /**
     * @return The API key of the Gemini API
     */
    public @Nullable String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey The API key of the Gemini API
     */
    public void setApiKey(@Nullable String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * @return Whether Vertex AI is used instead of the Gemini API
     */
    public boolean isVertexAi() {
        return vertexAi;
    }

    /**
     * @param vertexAi Whether Vertex AI is used instead of the Gemini API. Defaults to {@code false}.
     */
    public void setVertexAi(boolean vertexAi) {
        this.vertexAi = vertexAi;
    }

    /**
     * @return The Google Cloud project of Vertex AI
     */
    public @Nullable String getProjectId() {
        return projectId;
    }

    /**
     * @param projectId The Google Cloud project of Vertex AI
     */
    public void setProjectId(@Nullable String projectId) {
        this.projectId = projectId;
    }

    /**
     * @return The location of Vertex AI
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * @param location The location of Vertex AI, for example {@code us-central1} or {@code global}
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * @return The base URL of the API
     */
    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @param baseUrl The base URL of the API, for example a proxy. Defaults to the URL of the Gemini API or Vertex AI.
     */
    public void setBaseUrl(@Nullable String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @return The version of the API
     */
    public @Nullable String getApiVersion() {
        return apiVersion;
    }

    /**
     * @param apiVersion The version of the API, for example {@code v1}. Defaults to the SDK's version.
     */
    public void setApiVersion(@Nullable String apiVersion) {
        this.apiVersion = apiVersion;
    }

    /**
     * @return The timeout of the requests
     */
    public @Nullable Duration getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout of the requests
     */
    public void setTimeout(@Nullable Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * @return The maximum number of connections of the HTTP client
     */
    public @Nullable Integer getMaxConnections() {
        return maxConnections;
    }

    /**
     * @param maxConnections The maximum number of concurrent connections of the HTTP client
     */
    public void setMaxConnections(@Nullable Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    /**
     * @return The maximum number of connections of the HTTP client per host
     */
    public @Nullable Integer getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    /**
     * @param maxConnectionsPerHost The maximum number of concurrent connections of the HTTP client per host
     */
    public void setMaxConnectionsPerHost(@Nullable Integer maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }
}
