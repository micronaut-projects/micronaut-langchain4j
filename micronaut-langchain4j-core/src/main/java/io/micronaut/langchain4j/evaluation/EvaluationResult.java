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

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.NonNull;

import static io.micronaut.core.util.StringUtils.hasText;

/**
 * Represents the result of an evaluation.
 *
 * @param passing Whether the evaluation passed
 * @param feedback Short feedback from the evaluator
 * @since 2.1.1
 */
@Introspected
@Experimental
public record EvaluationResult(
    boolean passing,
    @NonNull String feedback
) {

    public EvaluationResult {
        feedback = normalize(feedback);
    }

    /**
     * Creates a passing result with the given feedback.
     *
     * @param feedback The feedback
     * @return The result
     * @since 2.1.1
     */
    @NonNull
    public static EvaluationResult pass(@NonNull String feedback) {
        return new EvaluationResult(true, feedback);
    }

    /**
     * Creates a failing result with the given feedback.
     *
     * @param feedback The feedback
     * @return The result
     * @since 2.1.1
     */
    @NonNull
    public static EvaluationResult fail(@NonNull String feedback) {
        return new EvaluationResult(false, feedback);
    }

    private static String normalize(String feedback) {
        return hasText(feedback) ? feedback.trim() : "No feedback provided.";
    }
}
