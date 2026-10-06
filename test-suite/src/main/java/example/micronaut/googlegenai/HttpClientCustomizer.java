package example.micronaut.googlegenai;

import com.google.genai.types.ClientOptions;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import okhttp3.OkHttpClient;

import java.time.Duration;

@Singleton
public class HttpClientCustomizer implements BeanCreatedEventListener<ClientOptions.Builder> { // <1>

    @Override
    public ClientOptions.Builder onCreated(BeanCreatedEvent<ClientOptions.Builder> event) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofMinutes(2))
            .build();
        return event.getBean().customHttpClient(httpClient); // <2>
    }
}
