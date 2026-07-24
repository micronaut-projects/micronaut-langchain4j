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
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailResult;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.service.guardrail.GuardrailService;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.langchain4j.annotation.AiService;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MicronautGuardrailServiceBuilderTest {

    static final String SPEC_NAME = "MicronautGuardrailServiceBuilderTest";

    @Test
    void resolvesMethodGuardrailsForMethodAndExecutableMethodKeys() throws NoSuchMethodException {
        try (ApplicationContext context = applicationContext()) {
            GuardrailInvocationTracker tracker = context.getBean(GuardrailInvocationTracker.class);
            BeanDefinition<MethodGuardedAssistant> beanDefinition = context.getBeanDefinition(MethodGuardedAssistant.class);
            GuardrailService service = new MicronautAiServiceContext(MethodGuardedAssistant.class, beanDefinition, context).guardrailService();

            Method inputMethod = MethodGuardedAssistant.class.getMethod("input", String.class);
            Method outputMethod = MethodGuardedAssistant.class.getMethod("output", String.class);
            Method plainMethod = MethodGuardedAssistant.class.getMethod("plain", String.class);
            ExecutableMethod<MethodGuardedAssistant, ?> executableInputMethod = beanDefinition.findMethod("input", String.class).orElseThrow();
            ExecutableMethod<MethodGuardedAssistant, ?> executableOutputMethod = beanDefinition.findMethod("output", String.class).orElseThrow();

            assertTrue(service.hasInputGuardrails(inputMethod));
            assertTrue(service.hasInputGuardrails(executableInputMethod));
            assertTrue(service.hasInputGuardrails("input(java.lang.String)"));
            assertFalse(service.hasInputGuardrails(plainMethod));
            assertFalse(service.hasInputGuardrails("plain(java.lang.String)"));
            assertTrue(service.hasOutputGuardrails(outputMethod));
            assertTrue(service.hasOutputGuardrails(executableOutputMethod));
            assertTrue(service.hasOutputGuardrails("output(java.lang.String)"));
            assertFalse(service.hasOutputGuardrails(plainMethod));
            assertFalse(service.hasOutputGuardrails("plain(java.lang.String)"));

            InputGuardrailResult inputResult = service.executeInputGuardrails(
                inputMethod,
                InputGuardrailRequest.builder()
                    .userMessage(UserMessage.from("hello"))
                    .commonParams(requestParams(MethodGuardedAssistant.class, "input"))
                    .build()
            );
            OutputGuardrailResult outputResult = service.executeOutputGuardrails(
                outputMethod,
                OutputGuardrailRequest.builder()
                    .responseFromLLM(ChatResponse.builder().aiMessage(AiMessage.from("pong")).build())
                    .chatExecutor(chatExecutor())
                    .requestParams(requestParams(MethodGuardedAssistant.class, "output"))
                    .build()
            );

            assertEquals(GuardrailResult.Result.SUCCESS, inputResult.result());
            assertEquals(GuardrailResult.Result.SUCCESS, outputResult.result());
            assertEquals(1, tracker.inputInvocations().get());
            assertEquals(1, tracker.outputInvocations().get());
        }
    }

    @Test
    void resolvesClassLevelInputGuardrailsFromBeanDefinition() throws NoSuchMethodException {
        try (ApplicationContext context = applicationContext()) {
            GuardrailInvocationTracker tracker = context.getBean(GuardrailInvocationTracker.class);
            BeanDefinition<ClassLevelGuardedAssistant> beanDefinition = context.getBeanDefinition(ClassLevelGuardedAssistant.class);
            GuardrailService service = new MicronautAiServiceContext(ClassLevelGuardedAssistant.class, beanDefinition, context).guardrailService();

            Method method = ClassLevelGuardedAssistant.class.getMethod("chat", String.class);

            assertTrue(service.hasInputGuardrails(method));

            InputGuardrailResult result = service.executeInputGuardrails(
                method,
                InputGuardrailRequest.builder()
                    .userMessage(UserMessage.from("hello"))
                    .commonParams(requestParams(ClassLevelGuardedAssistant.class, "chat"))
                    .build()
            );

            assertEquals(GuardrailResult.Result.SUCCESS, result.result());
            assertEquals(1, tracker.classLevelInputInvocations().get());
        }
    }

    @Test
    void resolvesClassLevelOutputGuardrailsFromBeanDefinition() throws NoSuchMethodException {
        try (ApplicationContext context = applicationContext()) {
            GuardrailInvocationTracker tracker = context.getBean(GuardrailInvocationTracker.class);
            BeanDefinition<ClassLevelOutputGuardedAssistant> beanDefinition = context.getBeanDefinition(ClassLevelOutputGuardedAssistant.class);
            GuardrailService service = new MicronautAiServiceContext(ClassLevelOutputGuardedAssistant.class, beanDefinition, context).guardrailService();

            Method method = ClassLevelOutputGuardedAssistant.class.getMethod("chat", String.class);

            assertTrue(service.hasOutputGuardrails(method));

            OutputGuardrailResult result = service.executeOutputGuardrails(
                method,
                OutputGuardrailRequest.builder()
                    .responseFromLLM(ChatResponse.builder().aiMessage(AiMessage.from("pong")).build())
                    .chatExecutor(chatExecutor())
                    .requestParams(requestParams(ClassLevelOutputGuardedAssistant.class, "chat"))
                    .build()
            );

            assertEquals(GuardrailResult.Result.SUCCESS, result.result());
            assertEquals(1, tracker.classLevelOutputInvocations().get());
        }
    }

    @Test
    void appliesBuilderConfiguredInputGuardrailsAcrossMethods() throws NoSuchMethodException {
        try (ApplicationContext context = applicationContext()) {
            GuardrailInvocationTracker tracker = context.getBean(GuardrailInvocationTracker.class);
            BeanDefinition<MethodGuardedAssistant> beanDefinition = context.getBeanDefinition(MethodGuardedAssistant.class);
            DirectInputGuard.reset();
            GuardrailService service = new MicronautGuardrailServiceBuilder(beanDefinition, context)
                .inputGuardrailsConfig(InputGuardrailsConfig.builder().build())
                .inputGuardrailClasses(List.of(DirectInputGuard.class))
                .build();

            Method plainMethod = MethodGuardedAssistant.class.getMethod("plain", String.class);

            assertTrue(service.hasInputGuardrails(plainMethod));

            InputGuardrailResult result = service.executeInputGuardrails(
                plainMethod,
                InputGuardrailRequest.builder()
                    .userMessage(UserMessage.from("hello"))
                    .commonParams(requestParams(MethodGuardedAssistant.class, "plain"))
                    .build()
            );

            assertEquals(GuardrailResult.Result.SUCCESS, result.result());
            assertEquals(1, DirectInputGuard.invocations().get());
            assertEquals(0, tracker.inputInvocations().get());
        }
    }

    @Test
    void appliesBuilderConfiguredOutputGuardrailsAcrossMethods() throws NoSuchMethodException {
        try (ApplicationContext context = applicationContext()) {
            GuardrailInvocationTracker tracker = context.getBean(GuardrailInvocationTracker.class);
            BeanDefinition<MethodGuardedAssistant> beanDefinition = context.getBeanDefinition(MethodGuardedAssistant.class);
            DirectOutputGuard.reset();
            GuardrailService service = new MicronautGuardrailServiceBuilder(beanDefinition, context)
                .outputGuardrailsConfig(OutputGuardrailsConfig.builder().build())
                .outputGuardrailClasses(List.of(DirectOutputGuard.class))
                .build();

            Method plainMethod = MethodGuardedAssistant.class.getMethod("plain", String.class);

            assertTrue(service.hasOutputGuardrails(plainMethod));

            OutputGuardrailResult result = service.executeOutputGuardrails(
                plainMethod,
                OutputGuardrailRequest.builder()
                    .responseFromLLM(ChatResponse.builder().aiMessage(AiMessage.from("pong")).build())
                    .chatExecutor(chatExecutor())
                    .requestParams(requestParams(MethodGuardedAssistant.class, "plain"))
                    .build()
            );

            assertEquals(GuardrailResult.Result.SUCCESS, result.result());
            assertEquals(1, DirectOutputGuard.invocations().get());
            assertEquals(0, tracker.outputInvocations().get());
        }
    }

    @Test
    void reusesBuiltGuardrailService() {
        try (ApplicationContext context = applicationContext()) {
            BeanDefinition<MethodGuardedAssistant> beanDefinition = context.getBeanDefinition(MethodGuardedAssistant.class);
            MicronautAiServiceContext aiServiceContext = new MicronautAiServiceContext(MethodGuardedAssistant.class, beanDefinition, context);

            var first = aiServiceContext.guardrailService();
            var second = aiServiceContext.guardrailService();

            assertSame(first, second);
        }
    }

    private static GuardrailRequestParams requestParams(Class<?> aiServiceType, String methodName) {
        InvocationContext invocationContext = InvocationContext.builder()
            .invocationId(UUID.randomUUID())
            .interfaceName(aiServiceType.getName())
            .methodName(methodName)
            .invocationParameters(new InvocationParameters())
            .timestamp(Instant.now())
            .build();

        return GuardrailRequestParams.builder()
            .userMessageTemplate("{{it}}")
            .variables(Map.of())
            .invocationContext(invocationContext)
            .build();
    }

    private static ApplicationContext applicationContext() {
        return ApplicationContext.run(Map.of("spec.name", SPEC_NAME));
    }

    private static ChatExecutor chatExecutor() {
        return new ChatExecutor() {
            @Override
            public ChatResponse execute() {
                return ChatResponse.builder().aiMessage(AiMessage.from("pong")).build();
            }

            @Override
            public ChatResponse execute(List<ChatMessage> chatMessages) {
                return ChatResponse.builder().aiMessage(AiMessage.from("pong")).build();
            }
        };
    }
}

@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
@AiService
interface MethodGuardedAssistant {
    @InputGuardrails(MethodInputGuard.class)
    String input(String userMessage);

    @OutputGuardrails(MethodOutputGuard.class)
    String output(String userMessage);

    String plain(String userMessage);
}

@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
@AiService
@InputGuardrails(ClassLevelInputGuard.class)
interface ClassLevelGuardedAssistant {
    String chat(String userMessage);
}

@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
@AiService
@OutputGuardrails(ClassLevelOutputGuard.class)
interface ClassLevelOutputGuardedAssistant {
    String chat(String userMessage);
}

@Singleton
@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
final class MethodInputGuard implements InputGuardrail {
    private final GuardrailInvocationTracker tracker;

    MethodInputGuard(GuardrailInvocationTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        tracker.inputInvocations().incrementAndGet();
        return success();
    }
}

@Singleton
@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
final class MethodOutputGuard implements OutputGuardrail {
    private final GuardrailInvocationTracker tracker;

    MethodOutputGuard(GuardrailInvocationTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        tracker.outputInvocations().incrementAndGet();
        return success();
    }
}

@Singleton
@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
final class ClassLevelInputGuard implements InputGuardrail {
    private final GuardrailInvocationTracker tracker;

    ClassLevelInputGuard(GuardrailInvocationTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        tracker.classLevelInputInvocations().incrementAndGet();
        return success();
    }
}

@Singleton
@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
final class ClassLevelOutputGuard implements OutputGuardrail {
    private final GuardrailInvocationTracker tracker;

    ClassLevelOutputGuard(GuardrailInvocationTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        tracker.classLevelOutputInvocations().incrementAndGet();
        return success();
    }
}

final class DirectInputGuard implements InputGuardrail {
    private static final AtomicInteger INVOCATIONS = new AtomicInteger();

    static AtomicInteger invocations() {
        return INVOCATIONS;
    }

    static void reset() {
        INVOCATIONS.set(0);
    }

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest request) {
        INVOCATIONS.incrementAndGet();
        return success();
    }
}

final class DirectOutputGuard implements OutputGuardrail {
    private static final AtomicInteger INVOCATIONS = new AtomicInteger();

    static AtomicInteger invocations() {
        return INVOCATIONS;
    }

    static void reset() {
        INVOCATIONS.set(0);
    }

    @Override
    public OutputGuardrailResult validate(OutputGuardrailRequest request) {
        INVOCATIONS.incrementAndGet();
        return success();
    }
}

@Singleton
@Requires(property = "spec.name", value = MicronautGuardrailServiceBuilderTest.SPEC_NAME)
final class GuardrailInvocationTracker {
    private final AtomicInteger inputInvocations = new AtomicInteger();
    private final AtomicInteger outputInvocations = new AtomicInteger();
    private final AtomicInteger classLevelInputInvocations = new AtomicInteger();
    private final AtomicInteger classLevelOutputInvocations = new AtomicInteger();

    AtomicInteger inputInvocations() {
        return inputInvocations;
    }

    AtomicInteger outputInvocations() {
        return outputInvocations;
    }

    AtomicInteger classLevelInputInvocations() {
        return classLevelInputInvocations;
    }

    AtomicInteger classLevelOutputInvocations() {
        return classLevelOutputInvocations;
    }
}
