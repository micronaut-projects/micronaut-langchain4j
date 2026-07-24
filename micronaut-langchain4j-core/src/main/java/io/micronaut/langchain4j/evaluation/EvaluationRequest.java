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
package io.micronaut.langchain4j.evaluation;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.Result;
import io.micronaut.core.annotation.Introspected;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.micronaut.core.util.StringUtils.hasText;

/**
 * Captures the prompt, optional context, and generated response for an evaluation.
 *
 * @param userText The original user text
 * @param context The optional grounding context
 * @param response The generated response
 * @since 2.1.1
 */
@Introspected
public record EvaluationRequest(
    @NonNull String userText,
    @Nullable String context,
    @NonNull String response
) {

    public EvaluationRequest {
        userText = requireText(userText, "userText");
        response = requireText(response, "response");
        context = hasText(context) ? context.trim() : null;
    }

    /**
     * Creates an evaluation request from the user text and AI service result.
     *
     * @param userText The original user text
     * @param result The AI service result
     * @return The evaluation request
     * @since 2.1.1
     */
    @NonNull
    public static EvaluationRequest from(@NonNull String userText, @NonNull Result<?> result) {
        Objects.requireNonNull(result, "result");
        return new EvaluationRequest(
            userText,
            join(result.sources()),
            Objects.toString(result.content(), "")
        );
    }

    /**
     * Creates an evaluation request from the user text, retrieved sources, and generated response.
     *
     * @param userText The original user text
     * @param sources The retrieved sources
     * @param response The generated response
     * @return The evaluation request
     * @since 2.1.1
     */
    @NonNull
    public static EvaluationRequest from(@NonNull String userText, @NonNull List<Content> sources, @NonNull String response) {
        return new EvaluationRequest(userText, join(sources), response);
    }

    private static String join(List<Content> sources) {
        if (sources == null || sources.isEmpty()) {
            return null;
        }
        return sources.stream()
            .map(Content::textSegment)
            .filter(Objects::nonNull)
            .map(segment -> segment.text())
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(text -> !text.isEmpty())
            .collect(Collectors.collectingAndThen(Collectors.joining("\n\n"), joined -> hasText(joined) ? joined : null));
    }

    private static String requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
