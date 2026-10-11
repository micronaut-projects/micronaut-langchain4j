package io.micronaut.langchain4j.test;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.micronaut.langchain4j.evaluation.EvaluationResult;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptedChatModelTest {

    @Test
    void respondsInOrderAndThenWithTheLastResponse() {
        ScriptedChatModel model = ScriptedChatModel.respondingWith("one", "two");
        assertEquals("one", model.chat("a"));
        assertEquals("two", model.chat("b"));
        assertEquals("two", model.chat("c"));
        assertEquals("c", ScriptedChatModel.lastUserMessage(model.lastRequest()));
        model.reset();
        assertTrue(model.requests().isEmpty());
        assertThrows(IllegalStateException.class, model::lastRequest);
    }

    @Test
    void requiresAResponse() {
        assertThrows(IllegalArgumentException.class, ScriptedChatModel::respondingWith);
    }

    @Test
    void readsTheMessagesOfTheRequest() {
        ChatRequest system = ChatRequest.builder().messages(SystemMessage.from("Be brief")).build();
        assertEquals("", ScriptedChatModel.lastUserMessage(system));
        assertEquals("", ScriptedChatModel.lastToolResult(system));

        UserMessage multimodal = UserMessage.from(TextContent.from("Describe"), ImageContent.from("https://example.com/cat.png"));
        ChatRequest request = ChatRequest.builder().messages(multimodal, AiMessage.from("Hello")).build();
        assertEquals(multimodal.contents().toString(), ScriptedChatModel.lastUserMessage(request));

        ToolExecutionRequest toolRequest = ToolExecutionRequest.builder().id("1").name("weather").arguments("{}").build();
        ChatRequest toolResult = ChatRequest.builder()
            .messages(UserMessage.from("Weather?"), AiMessage.from(toolRequest), ToolExecutionResultMessage.from(toolRequest, "Sunny"))
            .build();
        assertEquals("Sunny", ScriptedChatModel.lastToolResult(toolResult));
    }

    @Test
    void failsWhenNoRuleMatches() {
        ScriptedChatModel model = ScriptedChatModel.builder()
            .whenUserMessageContains("weather").respond("Sunny")
            .build();
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> model.chat("Hello"));
        assertEquals("No rule of the scripted chat model matches the request: Hello", error.getMessage());
    }

    @Test
    void streamsTheErrorsAndTheToolCalls() {
        ScriptedChatModel model = ScriptedChatModel.builder()
            .whenUserMessageContains("weather").callTool("weather", "{}")
            .build();

        CompletableFuture<ChatResponse> failed = stream(model, "Hello", new CopyOnWriteArrayList<>());
        CompletionException error = assertThrows(CompletionException.class, failed::join);
        assertInstanceOf(IllegalStateException.class, error.getCause());

        List<String> tokens = new CopyOnWriteArrayList<>();
        ChatResponse toolCall = stream(model, "What is the weather?", tokens).join();
        assertTrue(tokens.isEmpty());
        assertEquals("weather", toolCall.aiMessage().toolExecutionRequests().getFirst().name());
    }

    @Test
    void assertsTheResultOfAnEvaluation() {
        assertDoesNotThrow(() -> EvaluationAssertions.assertPasses(EvaluationResult.pass("Relevant")));
        EvaluationResult failed = EvaluationResult.fail("Off topic");
        AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> EvaluationAssertions.assertPasses(failed));
        assertEquals("The evaluation failed: Off topic", error.getMessage());
    }

    @Test
    void loadsTheSamplesOfAList() {
        List<EvaluationSample> samples = EvaluationSamples.load("/samples/list.yml");
        assertEquals(1, samples.size());
        assertEquals("sample 1", samples.getFirst().name());
        assertEquals("A JVM framework", samples.getFirst().expected());
        assertEquals(null, samples.getFirst().context());
    }

    @Test
    void rejectsInvalidSamples() {
        assertThrows(IllegalArgumentException.class, () -> EvaluationSamples.load("samples/missing.yml"));
        assertThrows(IllegalArgumentException.class, () -> EvaluationSamples.load("samples/not-a-list.yml"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> EvaluationSamples.load("samples/no-input.yml"));
        assertEquals("The evaluation sample 1 of samples/no-input.yml has no input", error.getMessage());
    }

    private static CompletableFuture<ChatResponse> stream(ScriptedChatModel model, String message, List<String> tokens) {
        CompletableFuture<ChatResponse> completed = new CompletableFuture<>();
        model.streaming().chat(message, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                tokens.add(partialResponse);
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                completed.complete(completeResponse);
            }

            @Override
            public void onError(Throwable error) {
                completed.completeExceptionally(error);
            }
        });
        return completed;
    }
}
