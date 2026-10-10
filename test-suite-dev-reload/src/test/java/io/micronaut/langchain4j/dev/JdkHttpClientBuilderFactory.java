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
package io.micronaut.langchain4j.dev;

import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;

/**
 * Hands the models the HTTP client of LangChain4j that is built on the JDK's, in place of the Micronaut HTTP client,
 * which resolves its client through the context. A model built on it holds nothing of the context, and is kept across
 * a restart.
 */
@Factory
@Requires(property = "test.http-client", value = "jdk")
public class JdkHttpClientBuilderFactory {

    /**
     * @return A builder of the JDK HTTP client
     */
    @Prototype
    HttpClientBuilder jdkHttpClientBuilder() {
        return JdkHttpClient.builder();
    }
}
