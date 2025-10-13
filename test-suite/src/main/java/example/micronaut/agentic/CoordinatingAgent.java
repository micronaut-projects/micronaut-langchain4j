package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.agentic.declarative.SubAgent;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Coordinator agent used in the integration test-suite.
 * Declarative workflow: first recommend a country, then suggest a recipe.
 */
@AgenticService(outputName = "recipe")
public interface CoordinatingAgent {

    // Declarative workflow definition (no method body needed)
    @SequenceAgent(
        subAgents = {
            @SubAgent(type = TravelRecommenderAgent.class, outputName = "country"),
            @SubAgent(type = RecipeAdvisorAgent.class, outputName = "recipe")
        },
        outputName = "recipe",
        name = "coordinate"
    )
    String coordinate(@V("topic") String topic);
}
