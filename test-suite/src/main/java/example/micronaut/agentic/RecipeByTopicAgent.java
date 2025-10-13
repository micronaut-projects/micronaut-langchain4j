package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Suggests a recipe given a topic/mood (topic-based to be usable in Parallel/Conditional flows).
 * Derived agent id: "recipe-by-topic".
 */
@AgenticService(outputName = "recipe")
public interface RecipeByTopicAgent {

    @UserMessage("""
        You are a cooking expert.
        Suggest a popular recipe that matches the topic "{{topic}}".
        Respond with the recipe name only: no punctuation, no explanations, just the recipe name.
        """)
    @Agent(description = "Suggests a popular recipe based on a topic")
    String suggest(@V("topic") String topic);
}
