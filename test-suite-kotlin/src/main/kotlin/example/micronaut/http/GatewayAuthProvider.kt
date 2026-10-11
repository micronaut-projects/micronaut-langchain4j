package example.micronaut.http

import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.langchain4j.http.client.ModelAuthProvider
import io.micronaut.langchain4j.http.client.ModelAuthRequest
import jakarta.inject.Singleton

@Singleton
@Requires(property = "ai-gateway.host") // <1>
class GatewayAuthProvider(
    @Value("\${ai-gateway.host}") private val gatewayHost: String
) : ModelAuthProvider {

    override fun authorization(request: ModelAuthRequest): String? {
        if (gatewayHost != request.uri().host) {
            return null // <2>
        }
        return "Bearer ${currentToken()}" // <3>
    }

    private fun currentToken(): String =
        // for example a short-lived token of a secret manager, refreshed when it expires
        System.getenv().getOrDefault("AI_GATEWAY_TOKEN", "")
}
