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

import java.util.Objects;

/**
 * Base class for evaluators that use a judge model.
 *
 * @since 2.1.1
 */
public abstract class PromptBasedEvaluator implements Evaluator {
    private final ChatModel chatModel;

    /**
     * @param chatModel The judge model
     * @since 2.0.0
     */
    protected PromptBasedEvaluator(@NonNull ChatModel chatModel) {
        this.chatModel = Objects.requireNonNull(chatModel, "chatModel");
    }

    @Override
    public @NonNull EvaluationResult evaluate(@NonNull EvaluationRequest request) {
        Objects.requireNonNull(request, "request");
        String response = chatModel.chat(buildPrompt(request));
        return parse(response);
    }

    /**
     * @return The evaluation name shown to the judge model
     * @since 2.0.0
     */
    @NonNull
    protected abstract String evaluationName();

    /**
     * @return The evaluation criteria shown to the judge model
     * @since 2.0.0
     */
    @NonNull
    protected abstract String evaluationCriteria();

    /**
     * Allows subclasses to validate the request before evaluation.
     *
     * @param request The request
     * @since 2.0.0
     */
    protected void validate(@NonNull EvaluationRequest request) {
        // no-op
    }

    private String buildPrompt(EvaluationRequest request) {
        validate(request);
        String context = request.context() == null ? "No context was provided." : request.context();
        return """
            You are running the Micronaut LangChain4j %s.
            Decide whether the AI response passes the evaluation.

            Return exactly two lines:
            PASS or FAIL
            A short explanation

            Evaluation criteria:
            %s

            User text:
            %s

            Context:
            %s

            AI response:
            %s
            """.formatted(
            evaluationName(),
            evaluationCriteria(),
            request.userText(),
            context,
            request.response()
        );
    }

    private EvaluationResult parse(String response) {
        String normalized = response == null ? "" : response.trim();
        if (normalized.isEmpty()) {
            return EvaluationResult.fail("The judge model returned an empty evaluation.");
        }
        String[] lines = normalized.split("\\R", 2);
        String verdict = lines[0].trim();
        String feedback = lines.length > 1 ? lines[1].trim() : verdict;

        if (verdict.equalsIgnoreCase("PASS")) {
            return EvaluationResult.pass(feedback);
        }
        if (verdict.equalsIgnoreCase("FAIL")) {
            return EvaluationResult.fail(feedback);
        }

        return EvaluationResult.fail("Unrecognized evaluation response: " + normalized);
    }
}
