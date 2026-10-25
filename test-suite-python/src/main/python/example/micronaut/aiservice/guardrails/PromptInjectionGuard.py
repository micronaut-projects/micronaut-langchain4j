from dev.langchain4j.guardrail import InputGuardrail, InputGuardrailRequest, InputGuardrailResult
from jakarta.inject import Singleton


@Singleton  # <1>
class PromptInjectionGuard(InputGuardrail):

    def validate(self, request: InputGuardrailRequest) -> InputGuardrailResult:  # <2>
        if "ignore previous instructions" in request.userMessage().singleText().lower():
            return self.fatal("Possible prompt injection")  # <3>
        return self.success()
