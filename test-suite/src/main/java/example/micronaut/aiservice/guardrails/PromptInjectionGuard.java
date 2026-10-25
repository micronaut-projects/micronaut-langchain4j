package example.micronaut.aiservice.guardrails;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.inject.Singleton;

@Singleton // <1>
public class PromptInjectionGuard implements InputGuardrail {

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) { // <2>
        if (request.userMessage().singleText().toLowerCase().contains("ignore previous instructions")) {
            return fatal("Possible prompt injection"); // <3>
        }
        return success();
    }
}
