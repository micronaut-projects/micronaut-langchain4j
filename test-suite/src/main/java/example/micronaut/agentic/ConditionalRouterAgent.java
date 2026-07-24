package example.micronaut.agentic;

import dev.langchain4j.agentic.declarative.ActivationCondition;
import dev.langchain4j.agentic.declarative.ConditionalAgent;
import dev.langchain4j.agentic.declarative.Output;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Demonstrates declarative ConditionalAgent orchestration using Micronaut DI.
 * Routes to RecipeByTopicAgent for food-related topics, otherwise to TravelResponseAgent.
 */
@AgenticService(outputKey = "response")
public interface ConditionalRouterAgent {

    @ConditionalAgent(
        subAgents = {
            RecipeByTopicAgent.class,
            TravelResponseAgent.class
        },
        outputKey = "response",
        name = "routeByTopic"
    )
    String route(@V("topic") String topic);

    @ActivationCondition(RecipeByTopicAgent.class)
    static boolean activateRecipe(@V("topic") String topic) {
        if (topic == null) {
            return false;
        }
        String t = topic.toLowerCase();
        return t.contains("food") || t.contains("cook") || t.contains("recipe") || t.contains("meal");
    }

    @ActivationCondition(TravelResponseAgent.class)
    static boolean activateTravel(@V("topic") String topic) {
        // Default branch when not food-related
        return !activateRecipe(topic);
    }

    @Output
    static String aggregate(@V("response") String response) {
        return response != null ? response.trim() : "";
    }
}
