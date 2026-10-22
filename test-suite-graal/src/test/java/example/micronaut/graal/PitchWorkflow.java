package example.micronaut.graal;

import dev.langchain4j.agentic.declarative.SequenceAgent;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

@AgenticService(outputKey = "pitch")
public interface PitchWorkflow {
    @SequenceAgent(subAgents = {AudienceAgent.class, PitchAgent.class}, outputKey = "pitch")
    String pitch(@V("topic") String topic);
}
