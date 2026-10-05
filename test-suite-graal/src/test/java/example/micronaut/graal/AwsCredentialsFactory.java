package example.micronaut.graal;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

@Factory
@Requires(property = "spec.name", value = "NativeModelProvidersTest")
class AwsCredentialsFactory {
    @Singleton
    AwsCredentialsProvider credentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));
    }
}
