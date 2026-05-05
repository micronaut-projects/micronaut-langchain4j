package example.micronaut.agentic;

import io.micronaut.langchain4j.testutils.OllamaTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers(disabledWithoutDocker = true)
@MicronautTest(startApplication = false)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SequenceAgentTest implements OllamaTestPropertyProvider {

    @Test
    void testManualCoordination(TravelRecommenderAgent travelRecommenderAgent, RecipeAdvisorAgent recipeAdvisorAgent) {
        var country = travelRecommenderAgent.recommendCountry("vegetarian food");
        var recipe = recipeAdvisorAgent.suggestRecipe(country);
        System.out.println("country = " + country);
        System.out.println("recipe = " + recipe);
        assertFalse(recipe.isEmpty());
    }

    @Test
    void testAutomaticSequence(CoordinatingAgent agent) {
        var result = agent.coordinate("astronomy");

        assertFalse(result.isEmpty());
        System.out.println(result);
    }

}
