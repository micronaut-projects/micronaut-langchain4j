package io.micronaut.langchain4j.evaluation;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBasedEvaluatorTest {

    @Test
    void parsesPassingJudgeResponse() {
        RecordingChatModel model = new RecordingChatModel("PASS\nThe response answers the user request.");
        RelevancyEvaluator evaluator = new RelevancyEvaluator(model);

        EvaluationResult result = evaluator.evaluate(new EvaluationRequest(
            "What is Micronaut?",
            "Micronaut is a JVM framework.",
            "Micronaut supports reactive programming."
        ));

        assertTrue(result.passing());
        assertEquals("The response answers the user request.", result.feedback());
        assertTrue(model.prompt().contains("What is Micronaut?"));
        assertTrue(model.prompt().contains("Context:\nMicronaut is a JVM framework."));
        assertTrue(model.prompt().contains("AI response:\nMicronaut supports reactive programming."));
        assertTrue(model.prompt().contains("directly answers the user text"));
    }

    @Test
    void parsesFailingJudgeResponse() {
        RecordingChatModel model = new RecordingChatModel("FAIL\nThe response ignores the user question.");
        RelevancyEvaluator evaluator = new RelevancyEvaluator(model);

        EvaluationResult result = evaluator.evaluate(new EvaluationRequest(
            "What is Micronaut?",
            null,
            "I like pizza."
        ));

        assertFalse(result.passing());
        assertEquals("The response ignores the user question.", result.feedback());
        assertTrue(model.prompt().contains("No context was provided."));
    }

    @Test
    void rejectsFactCheckingWithoutContext() {
        FactCheckingEvaluator evaluator = new FactCheckingEvaluator(new RecordingChatModel("PASS\nGrounded"));

        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(new EvaluationRequest(
            "What is Micronaut?",
            null,
            "Micronaut is a JVM framework."
        )));
    }

    @Test
    void failsOnUnexpectedJudgeResponse() {
        RelevancyEvaluator evaluator = new RelevancyEvaluator(new RecordingChatModel("MAYBE\nNeeds work"));

        EvaluationResult result = evaluator.evaluate(new EvaluationRequest(
            "What is Micronaut?",
            null,
            "Micronaut is a JVM framework."
        ));

        assertFalse(result.passing());
        assertTrue(result.feedback().startsWith("Unrecognized evaluation response:"));
    }

    private static final class RecordingChatModel implements ChatModel {
        private final String response;
        private String prompt;

        private RecordingChatModel(String response) {
            this.response = response;
        }

        @Override
        public ChatResponse doChat(ChatRequest chatRequest) {
            UserMessage userMessage = (UserMessage) chatRequest.messages().get(0);
            this.prompt = userMessage.singleText();
            return ChatResponse.builder()
                .aiMessage(new AiMessage(response))
                .build();
        }

        String prompt() {
            return prompt;
        }
    }
}
