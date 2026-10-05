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
import io.micronaut.langchain4j.evaluation.EvaluationRequest;
import org.jspecify.annotations.Nullable;

/**
 * A sample of an evaluation: the input sent to the AI service, with the optional context that the response must be
 * grounded in and the optional expected response, loaded from a sample file by {@link EvaluationSamples}.
 *
 * @param name The name of the sample, shown in the test reports
 * @param input The input sent to the AI service
 * @param context The context that the response must be grounded in, if any
 * @param expected The expected response, if any
 * @since 2.4.0
 */
@Experimental
public record EvaluationSample(String name, String input, @Nullable String context, @Nullable String expected) {

    /**
     * Creates the request that evaluates the response to this sample.
     *
     * @param response The response of the AI service
     * @return The evaluation request
     */
    public EvaluationRequest request(String response) {
        return new EvaluationRequest(input, context, response);
    }

    @Override
    public String toString() {
        return name;
    }
}
