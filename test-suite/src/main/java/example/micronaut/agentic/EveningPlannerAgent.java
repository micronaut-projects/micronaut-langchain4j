package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.SubAgent;
import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Declarative supervisor agent inspired by the "EveningPlannerAgent" example.
 * This agent coordinates sub-agents and produces a final "plan" output.
 */
@AgenticService(outputName = "plan")
public interface EveningPlannerAgent {

    // Declarative supervisor workflow definition (no method body needed)
    @SequenceAgent(
        subAgents = {
            @SubAgent(type = TravelRecommenderAgent.class, outputName = "country"),
            @SubAgent(type = RecipeAdvisorAgent.class, outputName = "recipe"),
            @SubAgent(type = PlanSynthesizerAgent.class, outputName = "plan")
        },
        outputName = "plan",
        name = "planEvening"
    )
    String plan(@V("topic") String topic);
}
