package example.micronaut.testing

import dev.langchain4j.model.chat.ChatModel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.langchain4j.evaluation.RelevancyEvaluator
import io.micronaut.langchain4j.test.EvaluationAssertions.assertPasses
import io.micronaut.langchain4j.test.EvaluationSample
import io.micronaut.langchain4j.test.EvaluationSamplesSource
import io.micronaut.langchain4j.test.ScriptedChatModel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import org.junit.jupiter.params.ParameterizedTest

@Property(name = "spec.name", value = "SupportAssistantEvaluationTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@Property(name = "langchain4j.evaluation.chat-model", value = "judge") // <1>
@MicronautTest(startApplication = false)
internal class SupportAssistantEvaluationTest {

    @Inject
    lateinit var assistant: SupportAssistant

    @Inject
    lateinit var relevancy: RelevancyEvaluator // <2>

    @ParameterizedTest
    @EvaluationSamplesSource("samples/support.yml") // <3>
    fun answersEachSample(sample: EvaluationSample) {
        val answer = assistant.answer(sample.input())
        assertPasses(sample, relevancy.evaluate(sample.request(answer))) // <4>
    }

    @Factory
    @Requires(property = "spec.name", value = "SupportAssistantEvaluationTest")
    class Models {

        @Bean
        @Primary
        fun assistantModel(): ChatModel = // <5>
            ScriptedChatModel.builder()
                .whenUserMessageContains("native").respond("Yes, with GraalVM.")
                .otherwise("Micronaut is a JVM framework for microservices.")
                .build()

        @Bean
        @Named("judge")
        fun judge(): ChatModel = ScriptedChatModel.respondingWith("PASS\nThe answer is relevant.")
    }
}
