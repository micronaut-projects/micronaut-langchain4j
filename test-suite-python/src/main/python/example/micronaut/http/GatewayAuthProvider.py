import os
from typing import Annotated

from micronaut.langchain4j.http.client import ModelAuthProvider, ModelAuthRequest
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires, Value


@Singleton
@Requires(property="ai-gateway.host")  # <1>
class GatewayAuthProvider(ModelAuthProvider):

    def __init__(self, gateway_host: Annotated[str, Value("${ai-gateway.host}")]):
        self.gateway_host = gateway_host

    def authorization(self, request: ModelAuthRequest) -> str | None:
        if self.gateway_host != request.uri().getHost():
            return None  # <2>
        return "Bearer " + self._current_token()  # <3>

    def _current_token(self) -> str:
        # for example a short-lived token of a secret manager, refreshed when it expires
        return os.environ.get("AI_GATEWAY_TOKEN", "")
