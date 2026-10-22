package example.micronaut.graal;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PitchAgent {
    @UserMessage("Pitch {{topic}} to {{audience}}")
    @Agent(outputKey = "pitch")
    String pitch(@V("topic") String topic, @V("audience") String audience);
}
