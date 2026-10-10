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
package io.micronaut.langchain4j.agentic;

import dev.langchain4j.agentic.declarative.DeclarativeUtil;
import dev.langchain4j.agentic.declarative.SupplierParameterResolver;
import dev.langchain4j.agentic.scope.AgenticScope;
import io.micronaut.context.BeanContext;
import io.micronaut.context.Qualifier;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Named;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves the parameters of the static LangChain4j supplier methods ({@code @ChatModelSupplier},
 * {@code @ToolsSupplier}, {@code @McpClientSupplier}, ...) as Micronaut beans, optionally qualified with
 * {@link Named}.
 *
 * <p>LangChain4j keeps a single global list of resolvers and resolves some suppliers lazily, when the agent is
 * invoked, so the resolver remembers the bean context that created each agent type.</p>
 */
@Internal
final class BeanContextSupplierParameterResolver implements SupplierParameterResolver {

    private static final BeanContextSupplierParameterResolver INSTANCE = new BeanContextSupplierParameterResolver();
    private static final ThreadLocal<BeanContext> CREATING = new ThreadLocal<>();

    static {
        DeclarativeUtil.addSupplierParameterResolver(INSTANCE);
    }

    private final Map<Class<?>, BeanContext> contexts = new ConcurrentHashMap<>();

    private BeanContextSupplierParameterResolver() {
    }

    /**
     * Runs the agent creation with the supplier parameters resolved from the given context.
     *
     * @param beanContext The bean context
     * @param creation The creation
     */
    static void creating(BeanContext beanContext, Runnable creation) {
        BeanContext previous = CREATING.get();
        CREATING.set(beanContext);
        try {
            creation.run();
        } finally {
            if (previous == null) {
                CREATING.remove();
            } else {
                CREATING.set(previous);
            }
        }
    }

    /**
     * Forgets the agent types created by a bean context that is being closed.
     *
     * @param beanContext The bean context
     */
    static void release(BeanContext beanContext) {
        INSTANCE.contexts.values().removeIf(context -> context == beanContext);
    }

    @Override
    public boolean supports(Context context) {
        BeanContext beanContext = CREATING.get();
        Parameter parameter = context.parameter();
        if (beanContext == null || !isBeanParameter(parameter)) {
            return false;
        }
        if (beanContext.containsBean(parameter.getType(), qualifier(parameter))) {
            contexts.put(context.declaringAgentClass(), beanContext);
            return true;
        }
        return false;
    }

    @Override
    public Object resolve(Context context) {
        BeanContext beanContext = CREATING.get();
        if (beanContext == null) {
            beanContext = contexts.get(context.declaringAgentClass());
        }
        if (beanContext == null) {
            throw new IllegalStateException("The bean context that created the agent " + context.declaringAgentClass().getName() + " is closed");
        }
        Parameter parameter = context.parameter();
        return beanContext.getBean(parameter.getType(), qualifier(parameter));
    }

    private static boolean isBeanParameter(Parameter parameter) {
        Class<?> type = parameter.getType();
        if (type == Object.class || type.isPrimitive() || type.getName().startsWith("java.lang.") || AgenticScope.class.isAssignableFrom(type)) {
            return false;
        }
        for (Annotation annotation : parameter.getAnnotations()) {
            // @V, @MemoryId, ... bind the agentic scope state
            if (annotation.annotationType().getName().startsWith("dev.langchain4j.")) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private static <T> Qualifier<T> qualifier(Parameter parameter) {
        Named named = parameter.getAnnotation(Named.class);
        return named != null ? Qualifiers.byName(named.value()) : null;
    }
}
