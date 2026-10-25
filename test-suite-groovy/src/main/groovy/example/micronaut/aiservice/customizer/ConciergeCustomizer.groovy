package example.micronaut.aiservice.customizer

import io.micronaut.langchain4j.aiservices.AiServiceCreationContext
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer
import jakarta.inject.Singleton

import java.util.function.UnaryOperator

@Singleton
class ConciergeCustomizer implements AiServiceCustomizer<Concierge> { // <1>

    @Override
    void customize(AiServiceCreationContext<Concierge> creationContext) {
        UnaryOperator<String> transformer = { String systemMessage -> systemMessage + " Keep your answers short." } as UnaryOperator<String>
        creationContext.aiServices().systemMessageTransformer(transformer) // <2>
    }
}
