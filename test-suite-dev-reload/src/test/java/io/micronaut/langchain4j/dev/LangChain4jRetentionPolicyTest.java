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

import dev.langchain4j.devtest.Holder;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.reload.BeanRetentionPolicy;
import io.micronaut.inject.BeanIdentifier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;

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

    @Test
    void whatTheRuntimeClasspathHoldsIsNotRefused() {
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decideHolding(new Holder(new int[] {1})));
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decideHolding(new Object[] {"text", Holder.class}));
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decideHolding(Optional.of(Holder.class.getClassLoader())));
        assertEquals(BeanRetentionPolicy.Decision.ABSTAIN, decideHolding(new AtomicReference<>("text")));
    }

    @Test
    void whatARestartReplacesIsRefusedWhereverItIsHeld() throws Exception {
        try (URLClassLoader application = new URLClassLoader(
            new URL[] {Holder.class.getProtectionDomain().getCodeSource().getLocation()},
            ClassLoader.getPlatformClassLoader())) {
            Class<?> type = application.loadClass(Holder.class.getName());
            Object instance = type.getConstructor(Object.class).newInstance((Object) null);

            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(application));
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(new Object[] {type}));
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(Optional.of(instance)));
            assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(new AtomicReference<>(instance)));
        }
    }

    @Test
    void aSingletonOfTheContextIsRefused() {
        Object singleton = new Holder("singleton");
        context.registerSingleton(singleton);

        assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(singleton));
    }

    @Test
    void tooManyObjectsAreRefused() {
        Object[] objects = new Object[200_002];
        Arrays.setAll(objects, i -> new Object());

        assertEquals(BeanRetentionPolicy.Decision.REFUSE, decideHolding(objects));
    }

    private static BeanRetentionPolicy.Decision decide(Executor executor) {
        return decide(HttpClient.newBuilder().executor(executor));
    }

    private static BeanRetentionPolicy.Decision decideHolding(Object value) {
        return new LangChain4jRetentionPolicy(context).decide(new BeanRegistration<>(BeanIdentifier.of("test"), null, new Holder(value)));
    }

    private static BeanRetentionPolicy.Decision decide(HttpClient.Builder client) {
        JdkHttpClient model = JdkHttpClient.builder().httpClientBuilder(client).build();
        return new LangChain4jRetentionPolicy(context).decide(new BeanRegistration<>(BeanIdentifier.of("test"), null, model));
    }
}
