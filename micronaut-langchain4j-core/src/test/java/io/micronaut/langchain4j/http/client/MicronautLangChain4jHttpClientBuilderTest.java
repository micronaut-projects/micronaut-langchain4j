/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.http.client;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpMethod;
import dev.langchain4j.http.client.HttpRequest;
import dev.langchain4j.http.client.SuccessfulHttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.reactivestreams.Publisher;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class MicronautLangChain4jHttpClientBuilderTest {
    @Test
    void usesMicronautManagedHttpClient(HttpClientBuilder builder, EmbeddedServer embeddedServer) {
        SuccessfulHttpResponse response = builder
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(5))
            .build()
            .execute(HttpRequest.builder()
                .method(HttpMethod.GET)
                .url(embeddedServer.getURL() + "/managed")
                .build());

        assertEquals("filtered", response.body());
    }

    @Filter("/managed")
    static class TestClientFilter implements HttpClientFilter {
        @Override
        public Publisher<? extends io.micronaut.http.HttpResponse<?>> doFilter(
            MutableHttpRequest<?> request,
            ClientFilterChain chain) {
            request.header("X-Micronaut-Client", "filtered");
            return chain.proceed(request);
        }
    }

    @Controller
    static class TestController {
        @Get("/managed")
        String managed(io.micronaut.http.HttpRequest<?> request) {
            return request.getHeaders().get("X-Micronaut-Client");
        }
    }
}
