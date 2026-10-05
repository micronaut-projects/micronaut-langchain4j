from typing import Annotated

from dev.langchain4j.model.chat import ChatModel
from jakarta.inject import Inject, Named
from micronaut.context.annotation import Bean, Factory, Primary, Property, Requires
from micronaut.langchain4j.evaluation import RelevancyEvaluator
from micronaut.langchain4j.test import EvaluationAssertions, EvaluationSamples, ScriptedChatModel
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.testing.SupportAssistant import SupportAssistant


@Property(name="spec.name", value="SupportAssistantEvaluationTest")
@Property(name="langchain4j.ollama.enabled", value="false")
@Property(name="langchain4j.evaluation.chat-model", value="judge")  # <1>
@MicronautTest(startApplication=False)
class SupportAssistantEvaluationTest:
    assistant: Annotated[SupportAssistant, Inject]
    relevancy: Annotated[RelevancyEvaluator, Inject]  # <2>

    @Test
    def answers_each_sample(self):
        for sample in EvaluationSamples.load("samples/support.yml"):  # <3>
            answer = self.assistant.answer(sample.input())
            EvaluationAssertions.assertPasses(sample, self.relevancy.evaluate(sample.request(answer)))  # <4>


@Requires(property="spec.name", value="SupportAssistantEvaluationTest")
@Factory
class Models:

    @Bean
    @Primary
    def assistant_model(self) -> ChatModel:  # <5>
        return ScriptedChatModel.builder() \
            .whenUserMessageContains("native").respond("Yes, with GraalVM.") \
            .otherwise("Micronaut is a JVM framework for microservices.") \
            .build()

    @Bean
    @Named("judge")
    def judge(self) -> ChatModel:
        return ScriptedChatModel.respondingWith("PASS\nThe answer is relevant.")
