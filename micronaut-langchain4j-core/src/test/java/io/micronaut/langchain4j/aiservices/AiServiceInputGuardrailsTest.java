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
import io.micronaut.langchain4j.annotation.AiService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false, transactional = false)
class AiServiceInputGuardrailsTest {

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

@AiService
interface GuardedAssistant {
    @InputGuardrails(PromptInjectionGuard.class)
    String chat(String userMessage);
}

@Singleton
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
final class GuardInvocationCounter {
    private final AtomicInteger invocations = new AtomicInteger();

    AtomicInteger invocations() {
        return invocations;
    }
}
