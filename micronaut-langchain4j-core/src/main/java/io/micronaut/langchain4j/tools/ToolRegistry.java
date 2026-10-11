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
package io.micronaut.langchain4j.tools;

import dev.langchain4j.agent.tool.CompensateFor;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.tool.AiServiceTool;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;

import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Register of tools.
 */
@Singleton
public class ToolRegistry implements ExecutableMethodProcessor<Tool> {
    private static final Logger LOG = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<BeanDefinition<?>, List<ExecutableMethod<?, ?>>> beansWithTools = new LinkedHashMap<>();
    private final Set<BeanDefinition<?>> compensationsReported = ConcurrentHashMap.newKeySet();
    private final BeanContext beanContext;

    public ToolRegistry(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public <B> void process(BeanDefinition<B> beanDefinition, ExecutableMethod<B, ?> method) {
        List<ExecutableMethod<?, ?>> methods = this.beansWithTools.computeIfAbsent(beanDefinition, definition -> new ArrayList<>());
        if (!methods.contains(method)) {
            methods.add(method);
        }
    }

    /**
     * Get all available tools.
     * @return The tools
     */
    public List<Object> getAllTools() {
        return this.beansWithTools.keySet().stream()
            .map(definition -> (Object) beanContext.getBean(definition))
            .toList();
    }

    /**
     * Get tools for the given types.
     * @param toolTypes The tool types.
     * @return A list of types
     */
    public List<Object> getToolsTyped(Set<?> toolTypes) {
        return this.beansWithTools.keySet().stream()
            .filter(definition -> toolTypes.contains(definition.getBeanType()))
            .map(definition -> (Object) beanContext.getBean(definition))
            .toList();
    }

    /**
     * Get the tools of the beans of the given types, as {@link AiServiceTool}s invoking the {@link Tool} methods through
     * their Micronaut {@link ExecutableMethod}: unlike the tool objects of {@link #getToolsTyped(Set)}, they let
     * LangChain4j call the tools without scanning and invoking the methods reflectively.
     *
     * @param toolTypes The tool types
     * @return The tools
     * @since 2.4.0
     */
    @NonNull
    public List<AiServiceTool> getAiServiceTools(@NonNull Set<?> toolTypes) {
        return aiServiceTools(definition -> toolTypes.contains(definition.getBeanType()));
    }

    /**
     * Get all available tools, as {@link AiServiceTool}s invoking the {@link Tool} methods through their Micronaut
     * {@link ExecutableMethod}.
     *
     * @return The tools
     * @since 2.4.0
     */
    @NonNull
    public List<AiServiceTool> getAllAiServiceTools() {
        return aiServiceTools(definition -> true);
    }

    private List<AiServiceTool> aiServiceTools(Predicate<BeanDefinition<?>> filter) {
        List<AiServiceTool> tools = new ArrayList<>();
        this.beansWithTools.forEach((definition, methods) -> {
            if (filter.test(definition)) {
                reportUnsupportedCompensations(definition);
                Object bean = beanContext.getBean(definition);
                for (ExecutableMethod<?, ?> method : methods) {
                    tools.add(ExecutableMethodToolExecutor.toAiServiceTool(bean, method));
                }
            }
        });
        return tools;
    }

    /**
     * LangChain4j only registers the {@link CompensateFor} compensating actions of the tool objects it scans, which
     * these tools replace: the actions never run, so it is reported once for each tool bean.
     */
    private void reportUnsupportedCompensations(BeanDefinition<?> definition) {
        List<ExecutableMethod<?, ?>> compensations = compensatingMethods(definition);
        if (!compensations.isEmpty() && compensationsReported.add(definition)) {
            for (ExecutableMethod<?, ?> method : compensations) {
                LOG.warn("@CompensateFor(\"{}\") on {}.{} is not supported and the compensating action never runs: "
                        + "LangChain4j only registers compensating actions for the tools it discovers reflectively",
                    method.stringValue(CompensateFor.class).orElse(""), definition.getBeanType().getName(), method.getMethodName());
            }
        }
    }

    /**
     * @param definition The definition of a tool bean
     * @return The {@link CompensateFor} methods of the bean
     */
    static List<ExecutableMethod<?, ?>> compensatingMethods(BeanDefinition<?> definition) {
        List<ExecutableMethod<?, ?>> methods = new ArrayList<>();
        for (ExecutableMethod<?, ?> method : definition.getExecutableMethods()) {
            if (method.hasAnnotation(CompensateFor.class)) {
                methods.add(method);
            }
        }
        return methods;
    }
}
