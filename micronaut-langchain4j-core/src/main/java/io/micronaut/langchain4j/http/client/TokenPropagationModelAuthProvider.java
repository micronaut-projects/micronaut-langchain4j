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

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.context.ServerHttpRequestContext;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Sends the token of the current HTTP request, as authenticated by Micronaut Security, to the models in place of their
 * API key. Enabled with {@code langchain4j.token-propagation.enabled}, optionally limited to the model URIs that match
 * {@code langchain4j.token-propagation.uri-regex}.
 *
 * @since 2.4.0
 */
@Internal
@Singleton
@Requires(property = TokenPropagationModelAuthProvider.PREFIX + ".enabled", value = StringUtils.TRUE)
final class TokenPropagationModelAuthProvider implements ModelAuthProvider {

    static final String PREFIX = "langchain4j.token-propagation";

    // the request attribute of the token authenticated by Micronaut Security (SecurityFilter.TOKEN)
    private static final String TOKEN = "micronaut.TOKEN";

    private final @Nullable Pattern uriPattern;

    TokenPropagationModelAuthProvider(@Value("${" + PREFIX + ".uri-regex:}") String uriRegex) {
        this.uriPattern = StringUtils.isNotEmpty(uriRegex) ? Pattern.compile(uriRegex) : null;
    }

    @Override
    public @Nullable String authorization(ModelAuthRequest request) {
        if (uriPattern != null && !uriPattern.matcher(request.uri().toString()).matches()) {
            return null;
        }
        return ServerHttpRequestContext.find()
            .flatMap(current -> current.getAttribute(TOKEN, String.class))
            .map(token -> "Bearer " + token)
            .orElse(null);
    }
}
