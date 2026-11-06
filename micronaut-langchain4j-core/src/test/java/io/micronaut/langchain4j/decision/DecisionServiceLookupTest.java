package io.micronaut.langchain4j.decision;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.service.decision.Decide;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.langchain4j.annotation.DecisionService;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A decision service uses the {@code DecisionModel} named after it, and fails clearly when none is found.
 */
class DecisionServiceLookupTest {

    @Test
    void usesTheDecisionModelNamedAfterTheService() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("spec.name", "DecisionServiceLookupTest.named"))) {
            assertTrue(context.getBean(SpamFilter.class).isSpam("Claim your free money now"));
        }
    }

    @Test
    void reportsAMissingDecisionModel() {
        try (ApplicationContext context = ApplicationContext.run(Map.of("spec.name", "DecisionServiceLookupTest.ambiguous"))) {
            SpamFilter filter = context.getBean(SpamFilter.class);
            ConfigurationException error = assertThrows(ConfigurationException.class, () -> filter.isSpam("Hello"));
            assertTrue(error.getMessage().contains("declare one named 'spam' or a default one"), error.getMessage());
        }
    }

    @Requires(property = "spec.name", pattern = "DecisionServiceLookupTest\\..*")
    @DecisionService("spam")
    interface SpamFilter {
        @Decide("Is this message spam?")
        boolean isSpam(String message);
    }

    @Factory
    @Requires(property = "spec.name", value = "DecisionServiceLookupTest.named")
    static class NamedModels {
        @Singleton
        @Named("spam")
        DecisionModel spam() {
            return new KeywordDecisionModel();
        }

        @Singleton
        @Named("other")
        DecisionModel other() {
            return new KeywordDecisionModel();
        }
    }

    /**
     * Two unqualified decision models and none named after the service: no model can be chosen.
     */
    @Factory
    @Requires(property = "spec.name", value = "DecisionServiceLookupTest.ambiguous")
    static class AmbiguousModels {
        @Singleton
        DecisionModel first() {
            return new KeywordDecisionModel();
        }

        @Singleton
        DecisionModel second() {
            return new KeywordDecisionModel();
        }
    }
}
