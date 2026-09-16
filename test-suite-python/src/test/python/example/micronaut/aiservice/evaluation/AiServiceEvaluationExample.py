from typing import Annotated

from dev.langchain4j.model.chat import ChatModel
from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.langchain4j.evaluation import EvaluationRequest, RelevancyEvaluator
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Disabled, Test

from example.micronaut.aiservice.evaluation.EvaluatingFriend import EvaluatingFriend


@Disabled("TODO(python): LangChain4j builds AI services reflectively from the Java interface")
@Property(name="spec.name", value="AiServiceEvaluationExample")
@MicronautTest(startApplication=False, environments=["ollama"])
class AiServiceEvaluationExample:
    friend: Annotated[EvaluatingFriend, Inject]
    chat_model: Annotated[ChatModel, Inject]

    @Test
    def evaluates_ai_service_response(self):
        user_text = "Reply with exactly: Micronaut is a JVM framework."
        response = self.friend.chat(user_text)

        evaluator = RelevancyEvaluator(self.chat_model)
        evaluation = evaluator.evaluate(EvaluationRequest.from_(user_text, response))

        assert evaluation is not None
        assert not evaluation.feedback().isBlank()
