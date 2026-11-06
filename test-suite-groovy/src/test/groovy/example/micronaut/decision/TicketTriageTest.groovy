package example.micronaut.decision

import dev.langchain4j.model.decision.DecisionModel
import dev.langchain4j.model.decision.request.ChoiceQuestion
import dev.langchain4j.model.decision.request.DecisionRequest
import dev.langchain4j.model.decision.response.ChoiceAnswer
import dev.langchain4j.model.decision.response.DecisionResponse
import dev.langchain4j.model.decision.response.YesNoAnswer
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

@Property(name = "spec.name", value = "TicketTriageTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false)
class TicketTriageTest {

    @Test
    void triagesTickets(TicketTriage triage) {
        assertFalse(triage.isSpam("I was charged twice"))
        assertEquals(Team.BILLING, triage.route("I was charged twice"))
        assertEquals(0.9d, triage.routeWithProbabilities("I was charged twice").probabilityOf(Team.BILLING))
    }

    /**
     * Answers no to the yes/no questions and chooses the first option of the other questions.
     */
    @Singleton
    @Requires(property = "spec.name", value = "TicketTriageTest")
    static class FirstOptionDecisionModel implements DecisionModel {
        @Override
        DecisionResponse doDecide(DecisionRequest request) {
            DecisionResponse.Builder response = DecisionResponse.builder()
            request.questions().each { name, question ->
                if (question instanceof ChoiceQuestion) {
                    String first = question.options().keySet().first()
                    ChoiceAnswer.Builder answer = ChoiceAnswer.builder().value(first).options(question.options().keySet())
                    question.options().keySet().each { answer.probability(it, it == first ? 0.9d : 0.1d) }
                    response.answer(name, answer.build())
                } else {
                    response.answer(name, YesNoAnswer.of(0.1d))
                }
            }
            response.build()
        }
    }
}
