/*
 * Copyright 2017-2021 original authors
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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry caching built agent proxies per agent interface.
 * Internal utility; not intended for extension.
 */
@Singleton
@Internal
public final class AgentRegistry {
    private final Map<Class<?>, Object> cachedAgents = new ConcurrentHashMap<>();

    /**
     * Cache the built agent proxy for the given agent interface.
     *
     * @param beanType the agent interface type
     * @param agent the agent proxy instance
     */
    public void putAgent(@NonNull Class<?> beanType, @NonNull Object agent) {
        cachedAgents.put(beanType, agent);
    }

    /**
     * Retrieve a cached agent proxy by agent interface type.
     *
     * @param beanType the agent interface type
     * @return the agent proxy instance if present, otherwise {@code null}
     */
    @Nullable
    public Object getAgent(@NonNull Class<?> beanType) {
        return cachedAgents.get(beanType);
    }
}
