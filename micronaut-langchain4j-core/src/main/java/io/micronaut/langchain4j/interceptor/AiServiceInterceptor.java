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

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.langchain4j.annotation.RegisterAiService;
import java.util.concurrent.ConcurrentHashMap;

@InterceptorBean(RegisterAiService.class)
public class AiServiceInterceptor implements MethodInterceptor<Object, Object> {
    private final ConcurrentHashMap<Class<Object>, Object> cachedAiServices = new ConcurrentHashMap<>();
    private final BeanContext beanContext;

    public AiServiceInterceptor(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        String name = context.stringValue(RegisterAiService.class).orElse(null);
        Class<Object> declaringType = context.getDeclaringType();
        Object target = cachedAiServices.get(declaringType);
        if (target == null) {
            ChatLanguageModel chatLanguageModel = beanContext.getBean(
                ChatLanguageModel.class,
                name != null ? Qualifiers.byName(name) : null
            );
            target = AiServices
                        .builder(declaringType)
                        .chatLanguageModel(chatLanguageModel)
                        .build();
            cachedAiServices.put(declaringType, target);
        }
        return context.getExecutableMethod().invoke(
            target,
            context.getParameterValues()
        );
    }
}
