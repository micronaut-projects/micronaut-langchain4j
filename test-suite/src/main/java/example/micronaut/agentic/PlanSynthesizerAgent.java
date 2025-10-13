package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Aggregates intermediate results into a final "plan".
 * Derived agent id: "plan-synthesizer".
 */
@AgenticService(outputName = "plan")
public interface PlanSynthesizerAgent {

    @UserMessage("""
        You are a helpful planner.
        Create a concise plan for an enjoyable evening in {{country}} that features the recipe "{{recipe}}".
        Respond with a single sentence plan with concrete, practical steps.
        """)
    @Agent(description = "Synthesizes a final evening plan from intermediate agent outputs")
    String synthesize(@V("country") String country, @V("recipe") String recipe);
}
