package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * A supervisor agent: the chat model plans which sub-agent to invoke next until the request is addressed.
 */
@AgenticService
public interface TravelSupervisorAgent {

    @SupervisorAgent(
        subAgents = {
            TravelRecommenderAgent.class,
            RecipeAdvisorAgent.class
        },
        responseStrategy = SupervisorResponseStrategy.SUMMARY,
        maxAgentsInvocations = 5
    )
    String organize(@V("request") String request);
}
