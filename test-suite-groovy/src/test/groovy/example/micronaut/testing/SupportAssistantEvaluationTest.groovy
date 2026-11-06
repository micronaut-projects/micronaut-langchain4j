package example.micronaut.testing

import dev.langchain4j.model.chat.ChatModel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator
import io.micronaut.langchain4j.test.EvaluationSample
import io.micronaut.langchain4j.test.EvaluationSamplesSource
import io.micronaut.langchain4j.test.ScriptedChatModel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import org.junit.jupiter.params.ParameterizedTest

import static io.micronaut.langchain4j.test.EvaluationAssertions.assertPasses

@Property(name = "spec.name", value = "SupportAssistantEvaluationTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@Property(name = "langchain4j.evaluation.chat-model", value = "judge") // <1>
@MicronautTest(startApplication = false)
class SupportAssistantEvaluationTest {

    @Inject
    SupportAssistant assistant

    @Inject
    RelevancyEvaluator relevancy // <2>

    @ParameterizedTest
    @EvaluationSamplesSource("samples/support.yml") // <3>
    void answersEachSample(EvaluationSample sample) {
        String answer = assistant.answer(sample.input())
        assertPasses(sample, relevancy.evaluate(sample.request(answer))) // <4>
    }

    @Factory
    @Requires(property = "spec.name", value = "SupportAssistantEvaluationTest")
    static class Models {

        @Bean
        @Primary
        ChatModel assistantModel() { // <5>
            ScriptedChatModel.builder()
                .whenUserMessageContains("native").respond("Yes, with GraalVM.")
                .otherwise("Micronaut is a JVM framework for microservices.")
                .build()
        }

        @Bean
        @Named("judge")
        ChatModel judge() {
            ScriptedChatModel.respondingWith("PASS\nThe answer is relevant.")
        }
    }
}
