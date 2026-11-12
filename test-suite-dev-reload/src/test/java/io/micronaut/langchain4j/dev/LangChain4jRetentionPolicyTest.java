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

import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.reload.BeanRetentionPolicy.Decision;
import io.micronaut.inject.BeanIdentifier;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The retention policy of the module decides from how a model was made, as the context knows it, and never reads what
 * LangChain4j built: it refuses a model made from a bean that a listener of the application customizes, or that
 * received what resolves through the stopped context, and abstains otherwise.
 */
class LangChain4jRetentionPolicyTest {

    @Test
    void aModelOnAClientThatHoldsNothingOfTheContextIsLeftToTheContext() {
        try (ApplicationContext context = context(Map.of("test.http-client", "jdk"))) {
            assertEquals(Decision.ABSTAIN, new LangChain4jRetentionPolicy(context).decide(registration(context)));
        }
    }

    @Test
    void aModelOnTheMicronautHttpClientIsRefusedSinceItsClientResolvesThroughTheContext() {
        try (ApplicationContext context = context(Map.of())) {
            assertEquals(Decision.REFUSE, new LangChain4jRetentionPolicy(context).decide(registration(context)));
        }
    }

    @Test
    void aModelWhoseBuilderAListenerOfTheApplicationCustomizesIsRefused() {
        try (ApplicationContext context = context(Map.of("test.http-client", "jdk", "test.builder-listener", "true"))) {
            BeanRegistration<?> registration = registration(context);
            // the listener of the test is of the runtime classpath
            assertEquals(Decision.ABSTAIN, new LangChain4jRetentionPolicy(context).decide(registration));
            // as a class of the application, which a restart replaces, it keeps the model from being retained
            assertEquals(Decision.REFUSE, new LangChain4jRetentionPolicy(context, type -> type == OllamaBuilderListener.class).decide(registration));
        }
    }

    @Test
    void aBeanThisModuleDoesNotRetainIsLeftToTheOtherPolicies() {
        try (ApplicationContext context = context(Map.of("test.http-client", "jdk"))) {
            LangChain4jRetentionPolicy policy = new LangChain4jRetentionPolicy(context, type -> true);
            assertEquals(Decision.ABSTAIN, policy.decide(context.findBeanRegistration(context.getBean(JdkHttpClientBuilderFactory.class)).orElseThrow()));
            assertEquals(Decision.ABSTAIN, policy.decide(new BeanRegistration<>(BeanIdentifier.of("test"), null, "not a model")));
        }
    }

    private static ApplicationContext context(Map<String, Object> properties) {
        Map<String, Object> all = new HashMap<>(properties);
        // the model is built, and never called
        all.put("langchain4j.ollama.base-url", "http://localhost:11434");
        all.put("langchain4j.ollama.model-name", "test");
        return ApplicationContext.builder()
            .properties(all)
            .beanDependencyTrackingEnabled(true)
            .start();
    }

    private static BeanRegistration<?> registration(ApplicationContext context) {
        return context.findBeanRegistration(context.getBean(ChatModel.class)).orElseThrow();
    }
}
