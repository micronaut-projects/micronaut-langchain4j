package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Declarative sequence workflow inspired by the "EveningPlannerAgent" example.
 * This agent coordinates sub-agents and produces a final "plan" output.
 */
@AgenticService(outputKey = "plan")
public interface EveningPlannerAgent {

    // Declarative sequence workflow definition (no method body needed)
    @SequenceAgent(
        subAgents = {
            TravelRecommenderAgent.class,
            RecipeAdvisorAgent.class,
            PlanSynthesizerAgent.class
        },
        outputKey = "plan",
        name = "planEvening"
    )
    String plan(@V("topic") String topic);
}
