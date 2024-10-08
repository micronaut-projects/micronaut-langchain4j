/*
 * Copyright 2017-2024 original authors
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

import dev.langchain4j.service.AiServices;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.langchain4j.aiservices.AiServiceCustomizer;
import io.micronaut.langchain4j.aiservices.AiServiceDef;
import io.micronaut.langchain4j.annotation.AiService;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor implementation for register AI service.
 */
@InterceptorBean(AiService.class)
public class AiServiceInterceptor implements MethodInterceptor<Object, Object> {
    private final ConcurrentHashMap<Class<Object>, Object> cachedAiServices = new ConcurrentHashMap<>();
    private final BeanContext beanContext;

    public AiServiceInterceptor(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        Object target = cachedAiService(context);
        return context.getExecutableMethod().invoke(target, context.getParameterValues());
    }

    private Object cachedAiService(MethodInvocationContext<Object, Object> context) {
        Class<Object> declaringType = context.getDeclaringType();
        Object target = cachedAiServices.get(declaringType);
        if (target != null) {
            return target;
        }
        AiServiceDef<Object> serviceDef = serviceDefinition(context);
        target = beanContext.createBean(AiServices.class, serviceDef).build();
        cachedAiServices.put(declaringType, target);
        return target;
    }

    private AiServiceDef<Object> serviceDefinition(MethodInvocationContext<Object, Object> context) {
        Class<Object> declaringType = context.getDeclaringType();
        AnnotationValue<AiService> annotation = context.getAnnotation(AiService.class);
        String name = annotation.stringValue("named").orElse(null);
        Set<Class<?>> tools = annotation.contains("tools") ? Set.of(annotation.classValues("tools")) : null;
        @SuppressWarnings("unchecked") Class<AiServiceCustomizer<Object>> customizer =
            (Class<AiServiceCustomizer<Object>>) annotation.classValue("customizer").orElse(null);

        BeanDefinition<Object> beanDefinition = beanContext.getBeanDefinition(declaringType);
        return new AiServiceDef<>(beanDefinition, declaringType, name, tools, customizer);
    }
}
