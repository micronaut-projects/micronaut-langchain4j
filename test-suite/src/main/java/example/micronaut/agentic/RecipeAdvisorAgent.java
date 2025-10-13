package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Suggests a popular recipe for a given country.
 * Derived agent id: "recipe-advisor".
 */
@AgenticService(outputName = "recipe")
public interface RecipeAdvisorAgent {

    @UserMessage("""
        You are a cooking expert.
        Suggest a popular recipe in {{country}}.
        Reply with the country name and the recipe name separated by a minus sign, then the details.
        """)
    @Agent(description = "Suggests a popular recipe based on a user request")
    String suggestRecipe(@V("country") String country);
}
