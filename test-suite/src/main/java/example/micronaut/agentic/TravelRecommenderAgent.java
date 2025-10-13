package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Recommends a country to visit based on a topic the user cares about.
 * Derived agent id: "travel-recommender".
 */
@AgenticService(outputName = "country")
public interface TravelRecommenderAgent {

    @UserMessage("""
    You are a travel expert and you know a lot about the culture of each country.
    The user is interested in {{topic}}, what's the best country to visit in order to entertain this?
    Your answer MUST be the country name only: no punctuation, no explanations, just the country name.
    """)
    @Agent(description = "Recommends a country to visit for a given user interest")
    String recommendCountry(@V("topic") String topic);
}
