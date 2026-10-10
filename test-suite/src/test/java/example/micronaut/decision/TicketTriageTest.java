package example.micronaut.decision;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.YesNoAnswer;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Property(name = "spec.name", value = "TicketTriageTest")
@Property(name = "langchain4j.ollama.enabled", value = "false")
@MicronautTest(startApplication = false)
class TicketTriageTest {

    @Test
    void triagesTickets(TicketTriage triage) {
        assertFalse(triage.isSpam("I was charged twice"));
        assertEquals(Team.BILLING, triage.route("I was charged twice"));
        assertEquals(0.9, triage.routeWithProbabilities("I was charged twice").probabilityOf(Team.BILLING));
    }

    /**
     * Answers no to the yes/no questions and chooses the first option of the other questions.
     */
    @Singleton
    @Requires(property = "spec.name", value = "TicketTriageTest")
    static class FirstOptionDecisionModel implements DecisionModel {
        @Override
        public DecisionResponse doDecide(DecisionRequest request) {
            DecisionResponse.Builder response = DecisionResponse.builder();
            request.questions().forEach((name, question) -> {
                if (question instanceof ChoiceQuestion choice) {
                    String first = choice.options().keySet().iterator().next();
                    ChoiceAnswer.Builder answer = ChoiceAnswer.builder().value(first).options(choice.options().keySet());
                    choice.options().keySet().forEach(option -> answer.probability(option, option.equals(first) ? 0.9 : 0.1));
                    response.answer(name, answer.build());
                } else {
                    response.answer(name, YesNoAnswer.of(0.1));
                }
            });
            return response.build();
        }
    }
}
