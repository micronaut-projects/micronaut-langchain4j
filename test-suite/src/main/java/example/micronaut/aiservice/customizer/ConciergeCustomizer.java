package example.micronaut.aiservice.customizer;

import io.micronaut.langchain4j.aiservices.AiServiceCreationContext;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import jakarta.inject.Singleton;

import java.util.function.UnaryOperator;

@Singleton
public class ConciergeCustomizer implements AiServiceCustomizer<Concierge> { // <1>

    @Override
    public void customize(AiServiceCreationContext<Concierge> creationContext) {
        UnaryOperator<String> transformer = systemMessage -> systemMessage + " Keep your answers short.";
        creationContext.aiServices().systemMessageTransformer(transformer); // <2>
    }
}
