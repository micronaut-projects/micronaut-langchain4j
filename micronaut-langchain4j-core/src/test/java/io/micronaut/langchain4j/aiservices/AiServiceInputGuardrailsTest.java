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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.guardrail.InputGuardrails;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = AiServiceInputGuardrailsTest.SPEC_NAME)
@MicronautTest(startApplication = false, transactional = false)
class AiServiceInputGuardrailsTest {

    static final String SPEC_NAME = "AiServiceInputGuardrailsTest";

    @Inject
    GuardedAssistant assistant;

    @Inject
    GuardInvocationCounter counter;

    @Test
    void resolvesInputGuardrailsFromMicronaut() {
        assertEquals("pong", assistant.chat("hello"));
        assertEquals(1, counter.invocations().get());
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC_NAME)
    static final class TestFactory {
        @Bean
        @Primary
        ChatModel chatModel() {
            return new ChatModel() {
                @Override
                public ChatResponse chat(ChatRequest chatRequest) {
                    return ChatResponse.builder()
                        .aiMessage(AiMessage.from("pong"))
                        .build();
                }
            };
        }
    }
}

@Requires(property = "spec.name", value = AiServiceInputGuardrailsTest.SPEC_NAME)
@AiService
interface GuardedAssistant {
    @InputGuardrails(PromptInjectionGuard.class)
    String chat(String userMessage);
}

@Singleton
@Requires(property = "spec.name", value = AiServiceInputGuardrailsTest.SPEC_NAME)
final class PromptInjectionGuard implements InputGuardrail {
    private final GuardInvocationCounter counter;

    PromptInjectionGuard(GuardInvocationCounter counter) {
        this.counter = counter;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        counter.invocations().incrementAndGet();
        return success();
    }
}

@Singleton
@Requires(property = "spec.name", value = AiServiceInputGuardrailsTest.SPEC_NAME)
final class GuardInvocationCounter {
    private final AtomicInteger invocations = new AtomicInteger();

    AtomicInteger invocations() {
        return invocations;
    }
}
