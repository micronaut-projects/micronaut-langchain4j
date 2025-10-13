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
package io.micronaut.langchain4j.agentic.tools;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.langchain4j.tools.ToolRegistry;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test replacement for ToolRegistry that tracks invocations of getToolsTyped.
 */
@Singleton
@Replaces(ToolRegistry.class)
final class CountingToolRegistry extends ToolRegistry {

    private final AtomicInteger invocations = new AtomicInteger();
    private volatile Set<?> lastRequestedTypes;

    CountingToolRegistry(BeanContext beanContext) {
        super(beanContext);
    }

    @Override
    public List<Object> getToolsTyped(Set<?> toolTypes) {
        lastRequestedTypes = toolTypes;
        invocations.incrementAndGet();
        return super.getToolsTyped(toolTypes);
    }

    int getInvocations() {
        return invocations.get();
    }

    Set<?> getLastRequestedTypes() {
        return lastRequestedTypes;
    }
}
