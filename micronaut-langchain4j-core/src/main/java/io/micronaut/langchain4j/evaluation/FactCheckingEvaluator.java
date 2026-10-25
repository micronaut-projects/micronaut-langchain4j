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

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.NonNull;

/**
 * Evaluates whether the generated response is grounded in the supplied context.
 *
 * @since 2.1.1
 */
@Experimental
public final class FactCheckingEvaluator extends PromptBasedEvaluator {

    /**
     * @param chatModel The judge model
     * @since 2.1.1
     */
    public FactCheckingEvaluator(@NonNull ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected void validate(@NonNull EvaluationRequest request) {
        if (request.context() == null) {
            throw new IllegalArgumentException("FactCheckingEvaluator requires non-empty context");
        }
    }

    @Override
    protected @NonNull String evaluationName() {
        return "fact checking evaluator";
    }

    @Override
    protected @NonNull String evaluationCriteria() {
        return "Pass only when every factual claim in the AI response is supported by the supplied context. Fail if the response hallucinates, contradicts the context, or omits critical grounding.";
    }
}
