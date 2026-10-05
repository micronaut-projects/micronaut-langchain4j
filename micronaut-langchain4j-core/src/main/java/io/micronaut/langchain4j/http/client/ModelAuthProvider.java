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

import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.Nullable;

/**
 * Provides the credentials of each request that a model sends with the Micronaut HTTP client, for example a token
 * of the current user or a short-lived token in place of the API key of the model configuration.
 *
 * <p>The {@code ModelAuthProvider} beans are asked in order: the first value that is not {@code null} replaces the
 * {@code Authorization} header of the request. A provider is called on the thread that sends the request, so it can
 * read the context of the current HTTP request.</p>
 *
 * @since 2.4.0
 */
@Experimental
@FunctionalInterface
public interface ModelAuthProvider {

    /**
     * Returns the value of the {@code Authorization} header of a request, for example {@code Bearer <token>}.
     *
     * @param request The request of the model
     * @return The value of the {@code Authorization} header, or {@code null} to keep the credentials of the model
     */
    @Nullable String authorization(ModelAuthRequest request);
}
