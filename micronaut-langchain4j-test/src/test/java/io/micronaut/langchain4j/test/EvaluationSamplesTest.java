package io.micronaut.langchain4j.test;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.langchain4j.evaluation.EvaluationResult;
import io.micronaut.langchain4j.evaluation.FactCheckingEvaluator;
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.micronaut.langchain4j.test.EvaluationAssertions.assertPasses;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = EvaluationSamplesTest.SPEC_NAME)
@Property(name = "langchain4j.evaluation.chat-model", value = "judge")
@MicronautTest(startApplication = false)
class EvaluationSamplesTest {

    static final String SPEC_NAME = "EvaluationSamplesTest";

    @Inject
    Assistant assistant;

    @Inject
    RelevancyEvaluator relevancy;

    @Inject
    FactCheckingEvaluator factChecking;

    @ParameterizedTest
    @EvaluationSamplesSource("samples/assistant.yml")
    void evaluatesEachSample(EvaluationSample sample) {
        String response = assistant.chat(sample.input());
        assertPasses(sample, relevancy.evaluate(sample.request(response)));
        assertPasses(sample, factChecking.evaluate(sample.request(response)));
    }

    @Test
    void loadsTheSamples() {
        List<EvaluationSample> samples = EvaluationSamples.load("classpath:samples/assistant.yml");
        assertEquals(List.of("framework", "native"), samples.stream().map(EvaluationSample::name).toList());
        assertEquals("Micronaut is a modern JVM framework for building microservices.", samples.getFirst().context());
    }

    @Test
    void reportsTheFeedbackOfAFailedEvaluation() {
        RelevancyEvaluator strict = new RelevancyEvaluator(ScriptedChatModel.respondingWith("FAIL\nOff topic"));
        EvaluationSample sample = EvaluationSamples.load("samples/assistant.yml").getFirst();
        EvaluationResult result = strict.evaluate(sample.request("Bananas"));
        AssertionError error = org.junit.jupiter.api.Assertions.assertThrows(AssertionError.class,
            () -> assertPasses(sample, result));
        assertEquals("The evaluation of the sample 'framework' failed: Off topic", error.getMessage());
    }

    @Test
    void scriptsToolCallsAndStreamsAndRecordsTheRequests() {
        ScriptedChatModel model = ScriptedChatModel.builder()
            .whenToolResult().respond(request -> "It is " + ScriptedChatModel.lastToolResult(request))
            .whenUserMessageContains("weather").callTool("weather", "{\"city\": \"Paris\"}")
            .otherwise("I do not know")
            .build();

        ChatResponse toolCall = model.chat(ChatRequest.builder().messages(UserMessage.from("What is the weather?")).build());
        assertEquals("weather", toolCall.aiMessage().toolExecutionRequests().getFirst().name());

        List<String> tokens = new CopyOnWriteArrayList<>();
        CompletableFuture<ChatResponse> completed = new CompletableFuture<>();
        model.streaming().chat("Hello there", new StreamingChatResponseHandler() {
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
        assertEquals(List.of("I ", "do ", "not ", "know"), tokens);
        assertEquals("I do not know", completed.join().aiMessage().text());
        assertEquals(List.of("What is the weather?", "Hello there"),
            model.requests().stream().map(ScriptedChatModel::lastUserMessage).toList());
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @AiService
    interface Assistant {
        String chat(String message);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class Models {
        @Bean
        @Primary
        ChatModel assistantModel() {
            return ScriptedChatModel.builder()
                .whenUserMessageContains("framework").respond("Use Micronaut, a modern JVM framework for microservices.")
                .whenUserMessageContains("native").respond("Yes, compile it with GraalVM.")
                .build();
        }

        @Bean
        @Named("judge")
        ChatModel judge() {
            return ScriptedChatModel.respondingWith("PASS\nThe response is relevant and grounded in the context.");
        }
    }
}
