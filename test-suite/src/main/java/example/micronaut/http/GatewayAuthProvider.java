package example.micronaut.http;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Value;
import io.micronaut.langchain4j.http.client.ModelAuthProvider;
import io.micronaut.langchain4j.http.client.ModelAuthRequest;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;

@Singleton
@Requires(property = "ai-gateway.host") // <1>
public class GatewayAuthProvider implements ModelAuthProvider {

    private final String gatewayHost;

    public GatewayAuthProvider(@Value("${ai-gateway.host}") String gatewayHost) {
        this.gatewayHost = gatewayHost;
    }

    @Override
    public @Nullable String authorization(ModelAuthRequest request) {
        if (!gatewayHost.equals(request.uri().getHost())) {
            return null; // <2>
        }
        return "Bearer " + currentToken(); // <3>
    }

    private String currentToken() {
        // for example a short-lived token of a secret manager, refreshed when it expires
        return System.getenv().getOrDefault("AI_GATEWAY_TOKEN", "");
    }
}
