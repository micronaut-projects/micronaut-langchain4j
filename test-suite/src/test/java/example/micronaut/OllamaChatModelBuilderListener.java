package example.micronaut;

import dev.langchain4j.model.ollama.OllamaChatModel;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.jspecify.annotations.NonNull;
import jakarta.inject.Singleton;

@Singleton
class OllamaChatModelBuilderListener
    implements BeanCreatedEventListener<OllamaChatModel.OllamaChatModelBuilder> {
    @Override
    public OllamaChatModel.OllamaChatModelBuilder onCreated(
        @NonNull BeanCreatedEvent<OllamaChatModel.OllamaChatModelBuilder> event) {
        OllamaChatModel.OllamaChatModelBuilder builder = event.getBean();
        builder.temperature(0.0);
        return builder;
    }
}
