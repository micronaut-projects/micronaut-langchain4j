package io.micronaut.langchain4j.decision;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.output.structured.Description;
import dev.langchain4j.service.decision.Choice;
import dev.langchain4j.service.decision.Decide;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.DecisionService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = DecisionServiceTest.SPEC_NAME)
@MicronautTest(startApplication = false)
class DecisionServiceTest {

    static final String SPEC_NAME = "DecisionServiceTest";

    @Inject
    SupportDesk supportDesk;

    @Test
    void answersYesNoQuestions() {
        assertTrue(supportDesk.isSpam("Claim your free money now"));
        assertFalse(supportDesk.isSpam("My invoice is wrong"));
    }

    @Test
    void choosesAnEnumConstant() {
        assertEquals(Team.BILLING, supportDesk.team("I was charged twice on my invoice"));
        Choice<Team> choice = supportDesk.route("The product crashes on startup");
        assertEquals(Team.SUPPORT, choice.value());
    }

    enum Team {
        @Description("Payments, invoices and refunds")
        BILLING,
        @Description("Problems using the product, crashes and errors")
        SUPPORT
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @DecisionService
    interface SupportDesk {
        @Decide("Is this message spam?")
        boolean isSpam(String message);

        @Decide("Which team should handle this ticket?")
        Team team(String ticket);

        @Decide("Which team should handle this ticket?")
        Choice<Team> route(String ticket);
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class DecisionModels {
        @Singleton
        DecisionModel decisionModel() {
            return new KeywordDecisionModel();
        }
    }
}
