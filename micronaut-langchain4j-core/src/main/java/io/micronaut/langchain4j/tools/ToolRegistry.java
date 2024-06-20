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

import dev.langchain4j.agent.tool.Tool;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ToolRegistry implements ExecutableMethodProcessor<Tool> {
    private final List<BeanDefinition<?>> beansWithTools = new ArrayList<>();
    private final BeanContext beanContext;

    public ToolRegistry(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (!this.beansWithTools.contains(beanDefinition)) {
            this.beansWithTools.add(beanDefinition);
        }
    }

    public List<Object> getAllTools() {
        return this.beansWithTools.stream()
            .map(definition -> (Object) beanContext.getBean(definition))
            .toList();
    }
}
