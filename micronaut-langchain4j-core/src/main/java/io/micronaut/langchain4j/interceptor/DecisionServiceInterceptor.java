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
package io.micronaut.langchain4j.interceptor;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.service.decision.DecisionServices;
import dev.langchain4j.service.decision.ThresholdProvider;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanProvider;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.exceptions.NonUniqueBeanException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.DecisionService;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the {@link DecisionService} interfaces with LangChain4j's {@link DecisionServices}.
 *
 * @since 2.4.0
 */
@Internal
@Experimental
@InterceptorBean(DecisionService.class)
public final class DecisionServiceInterceptor implements MethodInterceptor<Object, Object> {

    private final ConcurrentHashMap<Class<Object>, Object> decisionServices = new ConcurrentHashMap<>();
    private final BeanContext beanContext;

    DecisionServiceInterceptor(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        Class<Object> type = context.getDeclaringType();
        String name = context.stringValue(DecisionService.class, "named").filter(n -> !n.isBlank()).orElse(null);
        Object target = decisionServices.computeIfAbsent(type, t -> create(t, name));
        return context.getExecutableMethod().invoke(target, context.getParameterValues());
    }

    private Object create(Class<Object> type, @Nullable String name) {
        DecisionModel decisionModel = find(DecisionModel.class, name)
            .orElseThrow(() -> new ConfigurationException("No DecisionModel bean found for the decision service " + type.getName()
                + (name != null ? ": declare one named '" + name + "' or a default one" : "")));
        DecisionServices.Builder<Object> builder = DecisionServices.builder(type).decisionModel(decisionModel);
        find(ThresholdProvider.class, name).ifPresent(builder::thresholdProvider);
        return builder.build();
    }

    private <T> Optional<T> find(Class<T> beanType, @Nullable String name) {
        BeanProvider<T> provider = beanContext.getProvider(beanType);
        Optional<T> named = name != null ? provider.find(Qualifiers.byName(name)) : Optional.empty();
        if (named.isPresent()) {
            return named;
        }
        try {
            return provider.find(null);
        } catch (NonUniqueBeanException e) {
            return Optional.empty();
        }
    }
}
