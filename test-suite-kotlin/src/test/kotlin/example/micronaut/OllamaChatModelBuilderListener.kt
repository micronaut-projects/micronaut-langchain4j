package example.micronaut

import dev.langchain4j.model.ollama.OllamaChatModel.OllamaChatModelBuilder
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton

@Singleton
class OllamaChatModelBuilderListener : BeanCreatedEventListener<OllamaChatModelBuilder> {
    override fun onCreated(event: @NonNull BeanCreatedEvent<OllamaChatModelBuilder>): OllamaChatModelBuilder {
        val builder = event.bean
        builder.temperature(0.0)
        return builder
    }
}
