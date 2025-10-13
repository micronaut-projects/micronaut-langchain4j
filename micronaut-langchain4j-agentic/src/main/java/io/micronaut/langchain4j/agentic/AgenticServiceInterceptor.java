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
package io.micronaut.langchain4j.agentic;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.langchain4j.agentic.annotation.AgenticService;

import java.util.Set;

/**
 * Interceptor that builds and caches a LangChain4j declarative agentic service instance
 * from an interface annotated with {@link AgenticService}.
 *
 * Agents are created via {@link AgenticServiceFactory#buildAgenticService(BeanContext, AgenticServiceInfo)}
 * which delegates to LangChain4j's declarative API (AgenticServices.createAgenticSystem).
 */
@InterceptorBean(AgenticService.class)
public final class AgenticServiceInterceptor implements MethodInterceptor<Object, Object> {

    public static final String TOOLS_MEMBER = "tools";
    public static final String NAMED_MEMBER = "named";
    public static final String OUTPUT_NAME_MEMBER = "outputName";

    private final BeanContext beanContext;
    private final AgentRegistry agentRegistry;
    private final AgenticServiceFactory agenticServiceFactory;

    public AgenticServiceInterceptor(BeanContext beanContext,
                                     AgentRegistry agentRegistry,
                                     AgenticServiceFactory agenticServiceFactory) {
        this.beanContext = beanContext;
        this.agentRegistry = agentRegistry;
        this.agenticServiceFactory = agenticServiceFactory;
    }

    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        var target = cachedAgent(context);
        return context.getExecutableMethod().invoke(target, context.getParameterValues());
    }

    private Object cachedAgent(MethodInvocationContext<Object, Object> context) {
        var declaringType = context.getDeclaringType();
        var target = agentRegistry.getAgent(declaringType);
        if (target != null) {
            return target;
        }
        var annotation = context.getAnnotation(AgenticService.class);
        return resolveAgent(annotation, declaringType);
    }

    private Object resolveAgent(AnnotationValue<AgenticService> annotation,
                                Class<Object> declaringType) {
        var name = annotation != null ? annotation.stringValue(NAMED_MEMBER).orElse(null) : null;
        var tools = (annotation != null && annotation.contains(TOOLS_MEMBER))
            ? Set.of(annotation.classValues(TOOLS_MEMBER)) : null;
        var outputName = annotation != null && annotation.contains(OUTPUT_NAME_MEMBER)
            ? annotation.stringValue(OUTPUT_NAME_MEMBER).orElse(null) : null;
        if (outputName != null && outputName.trim().isEmpty()) {
            outputName = null;
        }

        var def = new AgenticServiceInfo<>(
            beanContext.getBeanDefinition(declaringType),
            declaringType,
            name,
            tools,
            outputName
        );

        var iface = resolveAgentInterface(declaringType);

        Object agent = agenticServiceFactory.buildAgenticService(beanContext, def);

        agentRegistry.putAgent(iface, agent);
        return agent;
    }

    /**
     * For Micronaut introduction proxies (e.g. GreeterAgent$Intercepted), return the original
     * agent interface annotated with {@link AgenticService}, otherwise return the given type.
     *
     * @param beanType The bean type that may be an intercepted class
     * @return The agent interface annotated with {@link AgenticService} or the provided type if already an interface
     */
    public static Class<?> resolveAgentInterface(Class<?> beanType) {
        if (beanType.isInterface()) {
            return beanType;
        }
        for (var iface : beanType.getInterfaces()) {
            if (iface.isAnnotationPresent(AgenticService.class)) {
                return iface;
            }
        }
        return beanType;
    }

}
