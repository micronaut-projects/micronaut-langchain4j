package example.micronaut.http;

import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.ClientFilter;
import io.micronaut.http.annotation.RequestFilter;

@ClientFilter("/v1/**") // <1>
public class RequestSourceFilter {

    @RequestFilter
    public void addSource(MutableHttpRequest<?> request) {
        request.header("X-Request-Source", "my-service"); // <2>
    }
}
