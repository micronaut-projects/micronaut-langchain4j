from typing import Annotated

from dev.langchain4j.model.decision import DecisionModel
from dev.langchain4j.model.decision.request import ChoiceQuestion, DecisionRequest
from dev.langchain4j.model.decision.response import ChoiceAnswer, DecisionResponse, YesNoAnswer
from jakarta.inject import Inject, Singleton
from micronaut.context.annotation import Property, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.decision.Team import Team
from example.micronaut.decision.TicketTriage import TicketTriage


@Property(name="spec.name", value="TicketTriageTest")
@Property(name="langchain4j.ollama.enabled", value="false")
@MicronautTest(startApplication=False)
class TicketTriageTest:
    triage: Annotated[TicketTriage, Inject]

    @Test
    def triages_tickets(self):
        assert not self.triage.is_spam("I was charged twice")
        assert self.triage.route("I was charged twice") == Team.BILLING
        assert self.triage.route_with_probabilities("I was charged twice").probabilityOf(Team.BILLING) == 0.9


# Answers no to the yes/no questions and chooses the first option of the other questions.
@Singleton
@Requires(property="spec.name", value="TicketTriageTest")
class FirstOptionDecisionModel(DecisionModel):

    def doDecide(self, request: DecisionRequest) -> DecisionResponse:
        response = DecisionResponse.builder()
        for name in request.questions().keySet():
            question = request.questions().get(name)
            if isinstance(question, ChoiceQuestion):
                options = list(question.options().keySet())
                answer = ChoiceAnswer.builder().value(options[0]).options(question.options().keySet())
                for option in options:
                    answer.probability(option, 0.9 if option == options[0] else 0.1)
                response.answer(name, answer.build())
            else:
                response.answer(name, YesNoAnswer.of(0.1))
        return response.build()
