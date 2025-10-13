package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.SubAgent;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Demonstrates declarative sequence orchestration using Micronaut DI.
 * Coordinates sub-agents and produces a final "plan".
 */
@AgenticService(outputName = "plan")
public interface SupervisingAgent {

    @SequenceAgent(
        subAgents = {
            @SubAgent(type = TravelRecommenderAgent.class, outputName = "country"),
            @SubAgent(type = RecipeAdvisorAgent.class, outputName = "recipe")
        },
        outputName = "plan",
        name = "superviseEvening"
    )
    String plan(@V("topic") String topic);



    @Output
    static String aggregate(@V("country") String country,
                            @V("recipe") String recipe) {
        String c = country != null ? country.trim() : "";
        String r = recipe != null ? recipe.trim() : "";
        if (!c.isEmpty() && !r.isEmpty()) {
            return "Plan: visit " + c + " and enjoy " + r + ".";
        }
        if (!c.isEmpty()) {
            return "Plan: visit " + c + ".";
        }
        if (!r.isEmpty()) {
            return "Plan: enjoy " + r + ".";
        }
        return "";
    }
}
