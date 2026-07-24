package example.micronaut.aiservice.evaluation;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.Result;
import io.micronaut.context.annotation.Property;
import io.micronaut.langchain4j.evaluation.EvaluationRequest;
import io.micronaut.langchain4j.evaluation.EvaluationResult;
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator;
import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Property(name = "spec.name", value = "AiServiceEvaluationExample")
@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AiServiceEvaluationExample implements OllamaTestPropertyProvider {

    @Test
    void evaluatesAiServiceResponse(EvaluatingFriend friend, ChatModel chatModel) {
        String userText = "Reply with exactly: Micronaut is a JVM framework.";
        Result<String> response = friend.chat(userText);

        RelevancyEvaluator evaluator = new RelevancyEvaluator(chatModel);
        EvaluationResult evaluation = evaluator.evaluate(EvaluationRequest.from(userText, response));

        assertNotNull(evaluation);
        assertFalse(evaluation.feedback().isBlank());
    }
}
