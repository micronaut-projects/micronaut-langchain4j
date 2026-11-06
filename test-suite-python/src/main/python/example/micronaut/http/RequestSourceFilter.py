from micronaut.http import MutableHttpRequest
from micronaut.http.annotation import ClientFilter, RequestFilter


@ClientFilter("/v1/**")  # <1>
class RequestSourceFilter:

    @RequestFilter
    def addSource(self, request: MutableHttpRequest) -> None:
        request.header("X-Request-Source", "my-service")  # <2>
