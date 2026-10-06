from com.google.genai.types import ClientOptions
from jakarta.inject import Singleton
from java.time import Duration
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener
from okhttp3 import OkHttpClient


@Singleton
class HttpClientCustomizer(BeanCreatedEventListener[ClientOptions.Builder]):  # <1>

    def onCreated(self, event: BeanCreatedEvent[ClientOptions.Builder]) -> ClientOptions.Builder:
        http_client = OkHttpClient.Builder() \
            .callTimeout(Duration.ofMinutes(2)) \
            .build()
        return event.getBean().customHttpClient(http_client)  # <2>
