package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Provides a travel-oriented response based on a topic that is NOT food-related.
 * Derived agent id: "travel-response".
 */
@AgenticService(outputName = "response")
public interface TravelResponseAgent {

    @UserMessage("""
        You are a travel expert.
        Suggest a single best country to visit for the topic "{{topic}}".
        Respond with the country name only: no punctuation, no explanations, just the country name.
        """)
    @Agent(description = "Suggests a country to visit for a non-food-related topic")
    String suggest(@V("topic") String topic);
}
