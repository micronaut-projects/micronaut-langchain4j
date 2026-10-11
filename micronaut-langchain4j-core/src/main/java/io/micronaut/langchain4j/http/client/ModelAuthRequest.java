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

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * A request of a model, passed to the {@link ModelAuthProvider} beans.
 *
 * @param method The HTTP method
 * @param uri The URI of the request
 * @param headers The headers of the request, with the credentials of the model configuration
 * @since 2.4.0
 */
@Experimental
public record ModelAuthRequest(String method, URI uri, Map<String, List<String>> headers) {
}
