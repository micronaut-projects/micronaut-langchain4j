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
package io.micronaut.langchain4j.aiservices;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.inject.BeanDefinition;
import java.util.Set;

/**
 * Models a AiService definition.
 *
 * @param beanDefinition The bean definition
 * @param type           The type
 * @param name           The name
 * @param tools          The tools
 * @param customizer     The customizer class
 * @param <T>            The generic type
 * @see io.micronaut.langchain4j.annotation.RegisterAiService
 */
public record AiServiceDef<T>(
    BeanDefinition<T> beanDefinition,
    @NonNull
    Class<T> type,
    @Nullable
    String name,
    @Nullable
    Set<Class<?>> tools,
    @Nullable
    Class<AiServiceCustomizer<T>> customizer
) {
}
