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
package io.micronaut.langchain4j.aiservices;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import org.jspecify.annotations.Nullable;

/**
 * Configuration of the AI services.
 *
 * <p>The settings under {@code langchain4j.ai-services.default} apply to every AI service.
 * The settings under {@code langchain4j.ai-services.<name>} apply to the AI service with that
 * {@link io.micronaut.langchain4j.annotation.AiService#named() name} and replace the default ones.</p>
 *
 * @since 2.4.0
 */
@EachProperty(AiServiceConfiguration.PREFIX)
public final class AiServiceConfiguration {

    /**
     * The configuration prefix.
     */
    public static final String PREFIX = "langchain4j.ai-services";

    /**
     * The name of the configuration that applies to every AI service.
     */
    public static final String DEFAULT = "default";

    private final String name;
    private @Nullable Integer maxToolCallingRoundTrips;
    private boolean executeToolsConcurrently;
    private @Nullable String toolExecutor;

    /**
     * @param name The name of the AI service, or {@value #DEFAULT}
     */
    public AiServiceConfiguration(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the AI service, or {@value #DEFAULT}
     */
    public String getName() {
        return name;
    }

    /**
     * @return The maximum number of tool calling round trips with the model per AI service call
     */
    public @Nullable Integer getMaxToolCallingRoundTrips() {
        return maxToolCallingRoundTrips;
    }

    /**
     * Sets the maximum number of tool calling round trips with the model per AI service call. Defaults to LangChain4j's limit.
     *
     * @param maxToolCallingRoundTrips The maximum number of round trips
     */
    public void setMaxToolCallingRoundTrips(@Nullable Integer maxToolCallingRoundTrips) {
        this.maxToolCallingRoundTrips = maxToolCallingRoundTrips;
    }

    /**
     * @return Whether the tools requested in a single model response are executed concurrently
     */
    public boolean isExecuteToolsConcurrently() {
        return executeToolsConcurrently;
    }

    /**
     * Sets whether the tools requested in a single model response are executed concurrently. Defaults to {@code false}.
     *
     * @param executeToolsConcurrently Whether to execute the tools concurrently
     * @see #setToolExecutor(String)
     */
    public void setExecuteToolsConcurrently(boolean executeToolsConcurrently) {
        this.executeToolsConcurrently = executeToolsConcurrently;
    }

    /**
     * @return The name of the executor that executes the tools concurrently
     */
    public @Nullable String getToolExecutor() {
        return toolExecutor;
    }

    /**
     * Sets the name of the Micronaut executor (for example {@code blocking} or {@code virtual}) that executes the tools
     * when {@link #isExecuteToolsConcurrently()} is enabled. Defaults to LangChain4j's executor.
     *
     * @param toolExecutor The executor name
     */
    public void setToolExecutor(@Nullable String toolExecutor) {
        this.toolExecutor = toolExecutor;
    }
}
