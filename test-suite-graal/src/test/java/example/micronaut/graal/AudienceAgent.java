package example.micronaut.graal;

import dev.langchain4j.agentic.declarative.HumanInTheLoop;
import dev.langchain4j.service.V;

/**
 * A human-in-the-loop sub-agent. It is a top-level type: the types nested in a test class are registered for
 * reflection by the JUnit support of the native image, whether or not metadata is generated for them.
 */
public interface AudienceAgent {
    @HumanInTheLoop(description = "Asks for the audience", outputKey = "audience")
    static String audience(@V("topic") String topic) {
        return "kids";
    }
}
