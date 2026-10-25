package example.micronaut.agentic;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

/**
 * Minimal typed Agentic service used by the test-suite to validate Micronaut integration.
 */
@AgenticService
public interface GreeterAgent {

    @UserMessage("Say hello to {{name}}")
    @Agent(description = "Greets a person by name")
    String greet(@V("name") String name);
}
