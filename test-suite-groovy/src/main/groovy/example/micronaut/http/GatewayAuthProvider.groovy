package example.micronaut.http

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.langchain4j.http.client.ModelAuthProvider
import io.micronaut.langchain4j.http.client.ModelAuthRequest
import jakarta.inject.Singleton

@Singleton
@Requires(property = "ai-gateway.host") // <1>
class GatewayAuthProvider implements ModelAuthProvider {

    private final String gatewayHost

    GatewayAuthProvider(@Value('${ai-gateway.host}') String gatewayHost) {
        this.gatewayHost = gatewayHost
    }

    @Override
    String authorization(ModelAuthRequest request) {
        if (gatewayHost != request.uri().host) {
            return null // <2>
        }
        "Bearer ${currentToken()}" // <3>
    }

    private static String currentToken() {
        // for example a short-lived token of a secret manager, refreshed when it expires
        System.getenv().getOrDefault("AI_GATEWAY_TOKEN", "")
    }
}
