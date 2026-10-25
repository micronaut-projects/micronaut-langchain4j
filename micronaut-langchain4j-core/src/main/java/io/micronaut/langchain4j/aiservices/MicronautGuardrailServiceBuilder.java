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

import dev.langchain4j.guardrail.Guardrail;
import dev.langchain4j.guardrail.GuardrailRequest;
import dev.langchain4j.guardrail.GuardrailResult;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.service.guardrail.AbstractGuardrailService;
import dev.langchain4j.service.guardrail.GuardrailService;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

final class MicronautGuardrailServiceBuilder implements GuardrailService.Builder {
    private final BeanDefinition<?> beanDefinition;
    private final Class<?> aiServiceClass;
    private final BeanContext beanContext;
    private InputGuardrailsConfig inputGuardrailsConfig;
    private OutputGuardrailsConfig outputGuardrailsConfig;
    private final List<Class<? extends InputGuardrail>> inputGuardrailClasses = new ArrayList<>();
    private final List<Class<? extends OutputGuardrail>> outputGuardrailClasses = new ArrayList<>();
    private final List<InputGuardrail> inputGuardrails = new ArrayList<>();
    private final List<OutputGuardrail> outputGuardrails = new ArrayList<>();

    MicronautGuardrailServiceBuilder(BeanDefinition<?> beanDefinition, BeanContext beanContext) {
        this.beanDefinition = beanDefinition;
        this.aiServiceClass = beanDefinition.getDeclaringType().orElse(beanDefinition.getBeanType());
        this.beanContext = beanContext;
    }

    @Override
    public GuardrailService.Builder inputGuardrailsConfig(InputGuardrailsConfig config) {
        this.inputGuardrailsConfig = config;
        return this;
    }

    @Override
    public GuardrailService.Builder outputGuardrailsConfig(OutputGuardrailsConfig config) {
        this.outputGuardrailsConfig = config;
        return this;
    }

    @Override
    public <I extends InputGuardrail> GuardrailService.Builder inputGuardrailClasses(List<Class<? extends I>> guardrailClasses) {
        this.inputGuardrailClasses.clear();
        if (guardrailClasses != null) {
            this.inputGuardrailClasses.addAll(guardrailClasses);
        }
        return this;
    }

    @Override
    public <O extends OutputGuardrail> GuardrailService.Builder outputGuardrailClasses(List<Class<? extends O>> guardrailClasses) {
        this.outputGuardrailClasses.clear();
        if (guardrailClasses != null) {
            this.outputGuardrailClasses.addAll(guardrailClasses);
        }
        return this;
    }

    @Override
    public <I extends InputGuardrail> GuardrailService.Builder inputGuardrails(List<I> guardrails) {
        this.inputGuardrails.clear();
        if (guardrails != null) {
            this.inputGuardrails.addAll(guardrails);
        }
        return this;
    }

    @Override
    public <O extends OutputGuardrail> GuardrailService.Builder outputGuardrails(List<O> guardrails) {
        this.outputGuardrails.clear();
        if (guardrails != null) {
            this.outputGuardrails.addAll(guardrails);
        }
        return this;
    }

    @Override
    public GuardrailService build() {
        Map<Object, InputGuardrailExecutor> inputGuardrailsByMethod = new HashMap<>();
        Map<Object, OutputGuardrailExecutor> outputGuardrailsByMethod = new HashMap<>();

        beanDefinition.getExecutableMethods().forEach(method -> {
                String methodKey = methodKey(method);
                InputGuardrailExecutor inputExecutor = computeInputGuardrailsForMethod(method);
                if (inputExecutor != null) {
                    inputGuardrailsByMethod.put(methodKey, inputExecutor);
                }
                OutputGuardrailExecutor outputExecutor = computeOutputGuardrailsForMethod(method);
                if (outputExecutor != null) {
                    outputGuardrailsByMethod.put(methodKey, outputExecutor);
                }
            });

        return new MicronautGuardrailService(aiServiceClass, inputGuardrailsByMethod, outputGuardrailsByMethod);
    }

    @Nullable
    private InputGuardrailExecutor computeInputGuardrailsForMethod(ExecutableMethod<?, ?> method) {
        if (inputGuardrailsAndConfigSetOnBuilder()) {
            return buildInputExecutor(inputGuardrailsConfig, getConfiguredInputGuardrails());
        }

        InputGuardrails annotation = annotation(method, InputGuardrails.class)
            .orElseGet(() -> annotation(beanDefinition, InputGuardrails.class).orElse(null));
        if (annotation != null) {
            return buildInputExecutor(
                hasInputGuardrailConfigSetOnBuilder() ? inputGuardrailsConfig : InputGuardrailsConfig.builder().build(),
                hasInputGuardrailsSetOnBuilder() ? getConfiguredInputGuardrails() : getInputGuardrails(annotation)
            );
        }
        return buildInputExecutor(inputGuardrailsConfig, getConfiguredInputGuardrails());
    }

    @Nullable
    private OutputGuardrailExecutor computeOutputGuardrailsForMethod(ExecutableMethod<?, ?> method) {
        if (outputGuardrailsAndConfigSetOnBuilder()) {
            return buildOutputExecutor(outputGuardrailsConfig, getConfiguredOutputGuardrails());
        }

        OutputGuardrails annotation = annotation(method, OutputGuardrails.class)
            .orElseGet(() -> annotation(beanDefinition, OutputGuardrails.class).orElse(null));
        if (annotation != null) {
            return buildOutputExecutor(
                hasOutputGuardrailConfigSetOnBuilder() ? outputGuardrailsConfig : OutputGuardrailsConfig.builder()
                    .maxRetries(annotation.maxRetries())
                    .build(),
                hasOutputGuardrailsSetOnBuilder() ? getConfiguredOutputGuardrails() : getOutputGuardrails(annotation)
            );
        }
        return buildOutputExecutor(outputGuardrailsConfig, getConfiguredOutputGuardrails());
    }

    @Nullable
    private InputGuardrailExecutor buildInputExecutor(@Nullable InputGuardrailsConfig config, List<InputGuardrail> guardrails) {
        if (guardrails.isEmpty()) {
            return null;
        }
        return InputGuardrailExecutor.builder()
            .config(config != null ? config : InputGuardrailsConfig.builder().build())
            .guardrails(guardrails)
            .build();
    }

    @Nullable
    private OutputGuardrailExecutor buildOutputExecutor(@Nullable OutputGuardrailsConfig config, List<OutputGuardrail> guardrails) {
        if (guardrails.isEmpty()) {
            return null;
        }
        return OutputGuardrailExecutor.builder()
            .config(config != null ? config : OutputGuardrailsConfig.builder().build())
            .guardrails(guardrails)
            .build();
    }

    private List<InputGuardrail> getConfiguredInputGuardrails() {
        return getConfiguredGuardrails(inputGuardrails, inputGuardrailClasses);
    }

    private List<OutputGuardrail> getConfiguredOutputGuardrails() {
        return getConfiguredGuardrails(outputGuardrails, outputGuardrailClasses);
    }

    private <P extends GuardrailRequest, R extends GuardrailResult<R>, G extends Guardrail<P, R>> List<G> getConfiguredGuardrails(
        List<G> guardrails,
        List<Class<? extends G>> guardrailClasses) {
        return Stream.concat(
                guardrails.stream(),
                guardrailClasses.stream().map(this::resolveGuardrail))
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<InputGuardrail> getInputGuardrails(InputGuardrails annotation) {
        return Arrays.stream(annotation.value())
            .map(guardrailClass -> (InputGuardrail) resolveGuardrail(guardrailClass))
            .toList();
    }

    @SuppressWarnings("unchecked")
    private List<OutputGuardrail> getOutputGuardrails(OutputGuardrails annotation) {
        return Arrays.stream(annotation.value())
            .map(guardrailClass -> (OutputGuardrail) resolveGuardrail(guardrailClass))
            .toList();
    }

    private <A extends java.lang.annotation.Annotation> Optional<A> annotation(
        AnnotationMetadataProvider metadataProvider,
        Class<A> annotationType) {
        return Optional.ofNullable(metadataProvider.synthesize(annotationType));
    }

    @SuppressWarnings("unchecked")
    private <P extends GuardrailRequest, R extends GuardrailResult<R>, G extends Guardrail<P, R>> G resolveGuardrail(
        Class<? extends G> guardrailClass) {
        Optional<? extends G> bean = beanContext.findBean(guardrailClass);
        if (bean.isPresent()) {
            return bean.get();
        }
        try {
            return guardrailClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                "No Micronaut bean of type [" + guardrailClass.getName() + "] found. " +
                "Register the guardrail class as a Micronaut bean (e.g. annotate it with @Singleton).", e);
        }
    }

    private static String methodKey(ExecutableMethod<?, ?> executableMethod) {
        return methodKey(executableMethod.getMethodName(), executableMethod.getArgumentTypes());
    }

    private static String methodKey(String methodName, Class<?>[] argumentTypes) {
        return methodName + "(" + Arrays.stream(argumentTypes).map(Class::getName).reduce((left, right) -> left + "," + right).orElse("") + ")";
    }

    private boolean hasInputGuardrailsSetOnBuilder() {
        return !inputGuardrails.isEmpty() || !inputGuardrailClasses.isEmpty();
    }

    private boolean hasInputGuardrailConfigSetOnBuilder() {
        return inputGuardrailsConfig != null;
    }

    private boolean inputGuardrailsAndConfigSetOnBuilder() {
        return hasInputGuardrailsSetOnBuilder() && hasInputGuardrailConfigSetOnBuilder();
    }

    private boolean hasOutputGuardrailsSetOnBuilder() {
        return !outputGuardrails.isEmpty() || !outputGuardrailClasses.isEmpty();
    }

    private boolean hasOutputGuardrailConfigSetOnBuilder() {
        return outputGuardrailsConfig != null;
    }

    private boolean outputGuardrailsAndConfigSetOnBuilder() {
        return hasOutputGuardrailsSetOnBuilder() && hasOutputGuardrailConfigSetOnBuilder();
    }

    private static final class MicronautGuardrailService extends AbstractGuardrailService {
        private MicronautGuardrailService(
            Class<?> aiServiceClass,
            Map<Object, InputGuardrailExecutor> inputGuardrails,
            Map<Object, OutputGuardrailExecutor> outputGuardrails) {
            super(aiServiceClass, inputGuardrails, outputGuardrails);
        }

        private static String methodKey(Object methodKey) {
            if (methodKey instanceof ExecutableMethod<?, ?> executableMethod) {
                return MicronautGuardrailServiceBuilder.methodKey(executableMethod);
            }
            if (methodKey instanceof java.lang.reflect.Method method) {
                return MicronautGuardrailServiceBuilder.methodKey(method.getName(), method.getParameterTypes());
            }
            return String.valueOf(methodKey);
        }

        @Override
        public <M> dev.langchain4j.guardrail.InputGuardrailResult executeInputGuardrails(M method, dev.langchain4j.guardrail.InputGuardrailRequest request) {
            return super.executeInputGuardrails(methodKey(method), request);
        }

        @Override
        public <M> dev.langchain4j.guardrail.OutputGuardrailResult executeOutputGuardrails(M method, dev.langchain4j.guardrail.OutputGuardrailRequest request) {
            return super.executeOutputGuardrails(methodKey(method), request);
        }

        @Override
        public <M> boolean hasInputGuardrails(M method) {
            return super.hasInputGuardrails(methodKey(method));
        }

        @Override
        public <M> boolean hasOutputGuardrails(M method) {
            return super.hasOutputGuardrails(methodKey(method));
        }
    }
}
