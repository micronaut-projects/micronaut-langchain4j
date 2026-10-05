package example.micronaut.aiservice.customizer

import io.micronaut.langchain4j.aiservices.AiServiceCreationContext
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer
import jakarta.inject.Singleton
import java.util.function.UnaryOperator

@Singleton
class ConciergeCustomizer : AiServiceCustomizer<Concierge> { // <1>

    override fun customize(creationContext: AiServiceCreationContext<Concierge>) {
        val transformer = UnaryOperator<String> { systemMessage -> "$systemMessage Keep your answers short." }
        creationContext.aiServices().systemMessageTransformer(transformer) // <2>
    }
}
