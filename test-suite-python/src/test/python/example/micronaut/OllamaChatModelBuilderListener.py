from dev.langchain4j.model.ollama import OllamaChatModel
from jakarta.inject import Singleton
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener


@Singleton
class OllamaChatModelBuilderListener(
    BeanCreatedEventListener[OllamaChatModel.OllamaChatModelBuilder]
):
    def onCreated(
        self, event: BeanCreatedEvent[OllamaChatModel.OllamaChatModelBuilder]
    ) -> OllamaChatModel.OllamaChatModelBuilder:
        builder = event.getBean()
        builder.temperature(0.0)
        return builder
