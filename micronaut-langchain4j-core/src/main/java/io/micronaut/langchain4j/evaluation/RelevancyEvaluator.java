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
import org.jspecify.annotations.NonNull;

/**
 * Evaluates whether the generated response is relevant to the user request.
 *
 * @since 2.0.0
 */
public final class RelevancyEvaluator extends PromptBasedEvaluator {

    /**
     * @param chatModel The judge model
     * @since 2.0.0
     */
    public RelevancyEvaluator(@NonNull ChatModel chatModel) {
        super(chatModel);
    }

    @Override
    protected @NonNull String evaluationName() {
        return "relevancy evaluator";
    }

    @Override
    protected @NonNull String evaluationCriteria() {
        return "Pass only when the AI response directly answers the user text and stays relevant to the supplied context.";
    }
}
