package example.micronaut.aiservice.evaluation;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.Result;
import io.micronaut.langchain4j.evaluation.EvaluationRequest;
import io.micronaut.langchain4j.evaluation.EvaluationResult;
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator;

final class AiServiceEvaluationExample {

    void evaluate(EvaluatingFriend friend, ChatModel chatModel) {
        Result<String> response = friend.chat("Hello");

        RelevancyEvaluator evaluator = new RelevancyEvaluator(chatModel);
        EvaluationResult evaluation = evaluator.evaluate(EvaluationRequest.from("Hello", response));

        if (!evaluation.passing()) {
            throw new AssertionError(evaluation.feedback());
        }
    }
}
