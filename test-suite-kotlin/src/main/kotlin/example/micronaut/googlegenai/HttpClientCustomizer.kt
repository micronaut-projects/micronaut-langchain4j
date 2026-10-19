package example.micronaut.googlegenai

import com.google.genai.types.ClientOptions
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import java.time.Duration

@Singleton
class HttpClientCustomizer : BeanCreatedEventListener<ClientOptions.Builder> { // <1>

    override fun onCreated(event: BeanCreatedEvent<ClientOptions.Builder>): ClientOptions.Builder {
        val httpClient = OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(2))
            .build()
        return event.bean.customHttpClient(httpClient) // <2>
    }
}
