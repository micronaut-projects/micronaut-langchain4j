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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

@Property(name = "spec.name", value = "TicketTriageTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false)
internal class TicketTriageTest {

    @Test
    fun triagesTickets(triage: TicketTriage) {
        assertFalse(triage.isSpam("I was charged twice"))
        assertEquals(Team.BILLING, triage.route("I was charged twice"))
        assertEquals(0.9, triage.routeWithProbabilities("I was charged twice").probabilityOf(Team.BILLING))
    }

    /**
     * Answers no to the yes/no questions and chooses the first option of the other questions.
     */
    @Singleton
    @Requires(property = "spec.name", value = "TicketTriageTest")
    class FirstOptionDecisionModel : DecisionModel {
        override fun doDecide(request: DecisionRequest): DecisionResponse {
            val response = DecisionResponse.builder()
            request.questions().forEach { (name, question) ->
                if (question is ChoiceQuestion) {
                    val first = question.options().keys.first()
                    val answer = ChoiceAnswer.builder().value(first).options(question.options().keys)
                    question.options().keys.forEach { answer.probability(it, if (it == first) 0.9 else 0.1) }
                    response.answer(name, answer.build())
                } else {
                    response.answer(name, YesNoAnswer.of(0.1))
                }
            }
            return response.build()
        }
    }
}
