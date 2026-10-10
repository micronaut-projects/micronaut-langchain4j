/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.langchain4j.test;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.langchain4j.evaluation.EvaluationResult;
import org.opentest4j.AssertionFailedError;

/**
 * Assertions of {@link EvaluationResult evaluation results}.
 *
 * @since 2.4.0
 */
@Experimental
public final class EvaluationAssertions {

    private EvaluationAssertions() {
    }

    /**
     * Asserts that an evaluation passed, reporting the feedback of the evaluator otherwise.
     *
     * @param result The result of the evaluation
     */
    public static void assertPasses(EvaluationResult result) {
        if (!result.passing()) {
            throw new AssertionFailedError("The evaluation failed: " + result.feedback());
        }
    }

    /**
     * Asserts that an evaluation of a sample passed, reporting the sample and the feedback of the evaluator otherwise.
     *
     * @param sample The sample
     * @param result The result of the evaluation
     */
    public static void assertPasses(EvaluationSample sample, EvaluationResult result) {
        if (!result.passing()) {
            throw new AssertionFailedError("The evaluation of the sample '" + sample.name() + "' failed: " + result.feedback());
        }
    }
}
