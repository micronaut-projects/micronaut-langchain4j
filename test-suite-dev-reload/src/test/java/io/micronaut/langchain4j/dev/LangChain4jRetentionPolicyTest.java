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

import dev.langchain4j.http.client.jdk.JdkHttpClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.inject.BeanIdentifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The executors of a JDK HTTP client, which a model may hold outside the injection graph, call back into what they were
 * created with: a model is not kept on an executor whose callbacks cannot be read.
 */
class LangChain4jRetentionPolicyTest {

    private static ApplicationContext context;

    @BeforeAll
    static void start() {
        context = ApplicationContext.run();
    }

    @AfterAll
    static void stop() {
        context.close();
    }

    @Test
    void aClientWithoutExecutorIsNotRefused() {
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decide(HttpClient.newBuilder()));
    }

    @Test
    void readableExecutorsAreNotRefused() {
        try (ExecutorService pool = Executors.newFixedThreadPool(1)) {
            assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decide(pool));
        }
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decide(ForkJoinPool.commonPool()));
    }

    @Test
    void aSingleThreadExecutorIsRefused() {
        try (ExecutorService wrapper = Executors.newSingleThreadExecutor()) {
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decide(wrapper));
        }
    }

    @Test
    void anUnconfigurableExecutorIsRefused() {
        try (ExecutorService pool = Executors.newFixedThreadPool(1);
             ExecutorService wrapper = Executors.unconfigurableExecutorService(pool)) {
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decide(wrapper));
        }
    }

    @Test
    void aThreadPerTaskExecutorIsRefused() {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decide(executor));
        }
    }

    private static BeanRetentionPolicy.Decision decide(Executor executor) {
        return decide(HttpClient.newBuilder().executor(executor));
    }

    private static BeanRetentionPolicy.Decision decide(HttpClient.Builder client) {
        JdkHttpClient model = JdkHttpClient.builder().httpClientBuilder(client).build();
        return new LangChain4jRetentionPolicy(context).decide(new BeanRegistration<>(BeanIdentifier.of("test"), null, model));
    }
}
